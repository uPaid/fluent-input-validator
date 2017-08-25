package validator;

import validator.ValidationConstraints.ValidationConstraint;
import validator.utils.Recorder;
import validator.utils.RecordingObject;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.stream;
import static java.util.Objects.*;
import static validator.ValidationConstraints.isNotNull;

/**
 * Provides fluent API for easy object validation.
 *
 * @author Paweł Fiuk
 * @see ValidationConstraints
 */

public final class FluentInputValidator<BaseObject> {

    private FluentInputValidator() {
    }

    private final ValidationMap validationResults = new ValidationMap();

    private BaseObject baseObject;
    private String baseObjectName;

    private FluentInputValidator(BaseObject baseObject) {
        this.baseObject = baseObject;
    }

    /**
     * Entry point for all validation.
     *
     * @param baseObject top-level object under validation
     *
     * @return new {@link FluentInputValidator.BaseObjectNameBuilder} for method chaining
     */
    public static <T> FluentInputValidator<T>.BaseObjectNameBuilder validate(T baseObject) {
        FluentInputValidator<T> validator = new FluentInputValidator<>(baseObject);
        validator.baseObject = baseObject;
        return validator.new BaseObjectNameBuilder();
    }

    /**
     * Provides a name for base object.
     */
    public class BaseObjectNameBuilder {
        private BaseObjectNameBuilder() {
        }

        /**
         * Uses top-level validation object's class name as it's name.
         *
         * @throws ValidationException if base object is null.
         */
        public final FieldValidatorBuilder withDefaultName() {
            checkBaseObject();
            FluentInputValidator.this.baseObjectName = baseObject.getClass()
                                                                 .getSimpleName();
            return new FieldValidatorBuilder();
        }

        /**
         * @param baseObjectName name of base object for validation map building
         *
         * @throws ValidationException if base object is null.
         */
        public final FieldValidatorBuilder as(String baseObjectName) {
            FluentInputValidator.this.baseObjectName = baseObjectName;
            checkBaseObject();
            return new FieldValidatorBuilder();
        }

        private void checkBaseObject() {
            if (isNull(baseObject)) {
                throw new ValidationException(baseObjectName + " may not be null");
            }
        }
    }

    /**
     * Entry point for field validation.
     */
    public class FieldValidatorBuilder {
        private FieldValidatorBuilder() {
        }

        /**
         * @param getter static method reference of field getter
         */
        public final <U extends FieldValidator<U, T>, T> FieldValidator<U, T> given(Function<BaseObject, T> getter) {
            Recorder<BaseObject> recorder = RecordingObject.create(getBaseObjectClass());
            getter.apply(recorder.getObject());
            return given(getter.apply(baseObject), recorder.getCurrentPropertyName());
        }

        /**
         * @param getter    instance method reference of field getter
         * @param fieldName name of field provided by the getter
         */
        public final <U extends FieldValidator<U, T>, T> FieldValidator<U, T> given(Supplier<T> getter, String fieldName) {
            return given(getter.get(), fieldName);
        }

        /**
         * @param field     base object's field
         * @param fieldName name of field provided by the getter
         */
        public final <U extends FieldValidator<U, T>, T> FieldValidator<U, T> given(T field, String fieldName) {
            return new FieldValidator<>(field, mergeFieldNames(baseObjectName, fieldName));
        }

        /**
         * {@link #given(Function)} variation for fields that implement {@link Iterable}.
         */
        public final <T> IterableFieldValidator<T, ? extends Iterable<T>> given(IterableFunction<BaseObject, T> getter) {
            Recorder<BaseObject> recorder = RecordingObject.create(getBaseObjectClass());
            getter.apply(recorder.getObject());
            return given(getter.apply(baseObject), recorder.getCurrentPropertyName());
        }

        /**
         * {@link #given(Supplier, String)} variation for fields that implement {@link Iterable}.
         */
        public final <T> IterableFieldValidator<T, ? extends Iterable<T>> given(IterableSupplier<T> getter, String fieldName) {
            return given(getter.get(), fieldName);
        }

        /**
         * {@link #given(Object, String)} variation for fields that implement {@link Iterable}.
         */
        public final <T> IterableFieldValidator<T, ? extends Iterable<T>> given(Iterable<T> field, String fieldName) {
            return new IterableFieldValidator<>(field, mergeFieldNames(baseObjectName, fieldName));
        }

        /**
         * @return endpoint for base object's validation.
         */
        public final ValidationFinalizer ifErrorsPresent() {
            return FluentInputValidator.this.new ValidationFinalizer();
        }
    }

    /**
     * {@link Function} variation for {@link Iterable} objects.
     */
    public interface IterableFunction<U, T> extends Function<U, Iterable<T>> {
    }

    /**
     * {@link Supplier} variation for {@link Iterable} objects.
     */
    public interface IterableSupplier<T> extends Supplier<Iterable<T>> {
    }

    /**
     * Extended {@link FieldValidator} for {@link Iterable} objects.
     */
    public class IterableFieldValidator<SubField, Field extends Iterable<SubField>> extends FieldValidator<IterableFieldValidator<SubField, Field>, Field> {
        private IterableFieldValidator(Field field, String fieldName) {
            super(field, fieldName);
        }

        /**
         * Allows validation for each element of an {@link Iterable} field.
         */
        public IterableFieldValidator<SubField, Field> forEach(Consumer<FluentInputValidator<Field>.FieldValidator<?, SubField>> validatorConsumer) {
            return forEach(String::valueOf, validatorConsumer);
        }

        /**
         * Allows validation for each element of an {@link Iterable} field.
         */
        public IterableFieldValidator<SubField, Field> forEach(Function<SubField, String> toString,
                                                               Consumer<FluentInputValidator<Field>.FieldValidator<?, SubField>> validatorConsumer) {
            andWhen(isNotNull());
            if (canBeValidated) {
                for (SubField subField : (nonNull(field) ? field : new ArrayList<SubField>())) {
                    FluentInputValidator<Field>.FieldValidator<?, SubField> subFieldValidator;
                    subFieldValidator = validate(field).as(fieldName)
                                                       .given(subField, nonNull(subField) ? toString.apply(subField) : "null");
                    validatorConsumer.accept(subFieldValidator);
                    validationResults.putAll(subFieldValidator.ifErrorsPresent()
                                                              .getValidationResults());
                }
            }
            return getGenericThis();
        }
    }

    /**
     * Allows validation of base object's fields.
     */
    public class FieldValidator<ThisType extends FieldValidator<ThisType, Field>, Field> {
        protected final Field field;
        protected final String fieldName;
        protected boolean canBeValidated = true;

        private FieldValidator(Field field, String fieldName) {
            this.field = field;
            this.fieldName = fieldName;
        }

        /**
         * Allows conditional validation. Erases all previous conditions for this field.
         */
        public final ThisType when(ValidationConstraint... validationConstraints) {
            canBeValidated = true;
            return andWhen(validationConstraints);
        }

        /**
         * Allows conditional validation. Erases all previous conditions for this field.
         */
        @SafeVarargs
        public final ThisType when(Function<Field, Boolean>... conditions) {
            canBeValidated = true;
            return andWhen(conditions);
        }

        /**
         * Allows conditional validation. Erases all previous conditions for this field.
         */
        public final ThisType when(Boolean... conditions) {
            canBeValidated = true;
            return andWhen(conditions);
        }

        /**
         * Allows conditional validation.
         */
        public final ThisType andWhen(ValidationConstraint... validationConstraints) {
            long conditionErrors = stream(validationConstraints).map(c -> c.getErrorFor(field))
                                                                .filter(Objects::nonNull)
                                                                .count();
            return andWhen(conditionErrors == 0);
        }

        /**
         * Allows conditional validation.
         */
        @SafeVarargs
        public final ThisType andWhen(Function<Field, Boolean>... conditions) {
            long conditionErrors = stream(conditions).map(f -> f.apply(field))
                                                     .filter(FALSE::equals)
                                                     .count();

            return andWhen(conditionErrors == 0);
        }

        /**
         * Allows conditional validation.
         */
        public final ThisType andWhen(Boolean... conditions) {
            boolean areAllConditionsMet = stream(conditions).reduce(Boolean::logicalAnd)
                                                            .orElse(false);
            canBeValidated = canBeValidated && areAllConditionsMet;
            return getGenericThis();
        }

        /**
         * Specifies validation constraints that a field will be validated against.
         */
        public final ThisType expectThat(ValidationConstraint... validationConstraints) {
            if (canBeValidated) {
                stream(validationConstraints).map(c -> c.getErrorFor(field))
                                             .filter(Objects::nonNull)
                                             .forEach(this::addValidationResult);
            }
            return getGenericThis();
        }

        /**
         * Allows validation of nested objects.
         */
        public ThisType validateInternals(Consumer<FluentInputValidator<Field>.FieldValidatorBuilder> validatorConsumer) {
            if (nonNull(field)) {
                FluentInputValidator<Field>.FieldValidatorBuilder validationBuilder = validate(field).as(fieldName);
                validatorConsumer.accept(validationBuilder);
                validationResults.putAll(validationBuilder.ifErrorsPresent()
                                                          .getValidationResults());
            }
            return getGenericThis();
        }

        /**
         * Allows custom validation at does not append errors, but possibly ends validation flow with an exception instead.
         */
        public ThisType validateUsing(Consumer<Field> consumer) {
            consumer.accept(field);
            return getGenericThis();
        }

        /**
         * Allows validation with {@link SpecializedValidator}.
         */
        public ThisType validateUsing(Supplier<SpecializedValidator<Field>> specializedValidatorSupplier) {
            return validateUsing(specializedValidatorSupplier.get());
        }

        /**
         * Allows validation with {@link SpecializedValidator}.
         */
        public ThisType validateUsing(SpecializedValidator<Field> specializedValidator) {
            if (nonNull(field)) {
                validationResults.putAll(specializedValidator.getValidationFor(field, fieldName));
            }
            return getGenericThis();
        }

        /**
         * Validation separator, allows validation of a different field.
         */
        public final FieldValidatorBuilder and() {
            return new FieldValidatorBuilder();
        }

        private void addValidationResult(String errorMessage) {
            String key = fieldName;

            List<String> currentValue = validationResults.get(key);
            if (isNull(currentValue)) {
                currentValue = new ArrayList<>();
                validationResults.put(key, currentValue);
            }

            currentValue.add(errorMessage);
        }

        /**
         * @return endpoint for validation.
         */
        public final ValidationFinalizer ifErrorsPresent() {
            return FluentInputValidator.this.new ValidationFinalizer();
        }

        @SuppressWarnings("unchecked")
        protected ThisType getGenericThis() {
            return (ThisType) this;
        }

    }

    /**
     * Endpoint for validation.
     */
    public class ValidationFinalizer {
        private ValidationFinalizer() {
        }

        /**
         * @return map containing field names and corresponding validation errors
         */
        public final ValidationMap getValidationResults() {
            return new ValidationMap(validationResults);
        }

        /**
         * @param exception to be thrown if validation errors are present
         */
        public final void throwException(RuntimeException exception) {
            throwIfNotNullAndValidationErrorOccurred(exception);
        }

        /**
         * @param exceptionSupplier supplier of exception to be thrown if validation errors are present
         */
        public final void throwException(Supplier<RuntimeException> exceptionSupplier) {
            throwIfNotNullAndValidationErrorOccurred(exceptionSupplier.get());
        }

        /**
         * @param exceptionFunction function that builds exception to be thrown from validation errors
         */
        public final void throwException(Function<ValidationMap, RuntimeException> exceptionFunction) {
            throwIfNotNullAndValidationErrorOccurred(exceptionFunction.apply(validationResults));

        }

        /**
         * @throws ValidationException if validation errors are present
         */
        public final void throwValidationException() {
            throwIfNotNullAndValidationErrorOccurred(null);
        }

        private <E extends RuntimeException> void throwIfNotNullAndValidationErrorOccurred(E exception) {
            if (!validationResults.isEmpty()) {
                if (nonNull(exception)) {
                    throw exception;
                }
                else {
                    throw getValidationException();
                }
            }
        }

        private ValidationException getValidationException() {
            return new ValidationException(validationResults.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private Class<BaseObject> getBaseObjectClass() {
        return (Class<BaseObject>) baseObject.getClass();
    }

    private String mergeFieldNames(String baseName, String fieldName) {
        return baseName + "." + fieldName;
    }

}
