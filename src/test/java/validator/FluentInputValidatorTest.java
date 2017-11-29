package validator;

import org.junit.Test;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;

import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static org.junit.Assert.*;
import static validator.FluentInputValidator.validate;
import static validator.ValidationConstraints.*;

public class FluentInputValidatorTest {

    @Test(expected = ValidationException.class)
    public void shouldThrowIfValidatedObjectIsNull() {
        validate(null).as("null")
                      .ifErrorsPresent()
                      .throwValidationException();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowAnExceptionFromCustomValidator() {
        validate(new Object()).withDefaultName()
                              .given(identity(), "")
                              .validateUsing(f -> {throw new IndexOutOfBoundsException();})
                              .ifErrorsPresent()
                              .throwValidationException();
    }

    @Test
    public void shouldNotThrowEvenIfNoDefaultConstructorIsPresentInInnerObject() {
        ClassUnderTestWithNoNullConstructorInnerObject testObject;
        testObject = new ClassUnderTestWithNoNullConstructorInnerObject(new ClassUnderTestWithNoNullConstructor(null));

        validate(testObject).withDefaultName()
                            .given(ClassUnderTestWithNoNullConstructorInnerObject::getInnerObject)
                            .expectThat(isNotNull())
                            .ifErrorsPresent()
                            .throwValidationException();
    }

    @Test
    public void shouldAllowIteratingOverACollection() {
        ClassUnderTestWithIterable testObject = new ClassUnderTestWithIterable(asList(null, " ", "test"));

        ValidationMap validation;
        validation = validate(testObject).withDefaultName()
                                         .given(ClassUnderTestWithIterable::getList)
                                         .forEach(element -> element.expectThat(isNotNull(),
                                                                                isNotEmpty(),
                                                                                isNotWhitespace()))
                                         .ifErrorsPresent()
                                         .getValidationResults();

        assertTrue(validation.containsKey("ClassUnderTestWithIterable.list.null"));
        assertTrue(validation.containsKey("ClassUnderTestWithIterable.list. "));
        assertFalse(validation.containsKey("ClassUnderTestWithIterable.list.test"));
    }

    @Test
    public void shouldAllowValidationOfInnerObjects() {
        ClassUnderTestComplex testObject = new ClassUnderTestComplex(new ClassUnderTestSimple(null));

        ValidationMap validation;
        validation = validate(testObject).withDefaultName()
                                         .given(ClassUnderTestComplex::getInnerObject)
                                         .validateInternals(v -> v.given(ClassUnderTestSimple::getVariable)
                                                                  .expectThat(isNotNull()))
                                         .ifErrorsPresent()
                                         .getValidationResults();

        assertTrue(validation.containsKey("ClassUnderTestComplex.innerObject.variable"));
    }

    @Test
    public void shouldAllowConditionalValidation() {
        ClassUnderTestComplex testObject = new ClassUnderTestComplex(null);

        validate(testObject).withDefaultName()
                            .given(ClassUnderTestComplex::getInnerObject)
                            .when(FALSE)
                            .expectThat(isNotNull())
                            .ifErrorsPresent()
                            .throwValidationException();

        validate(testObject).withDefaultName()
                            .given(ClassUnderTestComplex::getInnerObject)
                            .when(isNotNull())
                            .expectThat(isNotNull())
                            .ifErrorsPresent()
                            .throwValidationException();

        ValidationMap validation;
        validation = validate(testObject).withDefaultName()
                                         .given(ClassUnderTestComplex::getInnerObject)
                                         .when(Objects::isNull)
                                         .expectThat(isNotNull())
                                         .ifErrorsPresent()
                                         .getValidationResults();

        assertTrue(validation.containsKey("ClassUnderTestComplex.innerObject"));
    }

    @Test
    public void shouldAllowValidationWithSeparateValidator() {
        ClassUnderTestComplex testObject = new ClassUnderTestComplex(new ClassUnderTestSimple(null));

        ValidationMap validation;
        validation = validate(testObject).withDefaultName()
                                         .given(ClassUnderTestComplex::getInnerObject)
                                         .validateUsing(ClassUnderTestSimpleValidator::new)
                                         .ifErrorsPresent()
                                         .getValidationResults();

        assertTrue(validation.containsKey("ClassUnderTestComplex.innerObject.variable"));
    }

    @Test
    public void shouldCheckWithPredicate() {
        ValidationMap validation;
        validation = validate(new ClassUnderTestSimple(1))
                .withDefaultName()
                .given(ClassUnderTestSimple::getVariable)
                .expectThat(fulfills(Integer.valueOf(2)::equals))
                .expectThat(fulfills(i -> {throw new RuntimeException();}))
                .ifErrorsPresent()
                .getValidationResults();

        assertTrue(validation.containsKey("ClassUnderTestSimple.variable"));
        assertEquals(2,
                     validation.get("ClassUnderTestSimple.variable")
                               .size());
    }

    private class ClassUnderTestSimpleValidator implements SpecializedValidator<ClassUnderTestSimple> {
        @Override
        public ValidationMap getValidationFor(ClassUnderTestSimple input, String inputName) {
            return validate(input).as(inputName)
                                  .given(ClassUnderTestSimple::getVariable)
                                  .expectThat(isNotNull())
                                  .ifErrorsPresent()
                                  .getValidationResults();
        }
    }

    private static class ClassUnderTestWithIterable {
        private List<?> list;

        public ClassUnderTestWithIterable() {
        }

        public ClassUnderTestWithIterable(List<?> list) {
            this.list = list;
        }

        public List<?> getList() {
            return list;
        }
    }

    private static class ClassUnderTestComplex {
        private ClassUnderTestSimple innerObject;

        public ClassUnderTestComplex() {
        }

        public ClassUnderTestComplex(ClassUnderTestSimple innerObject) {
            this.innerObject = innerObject;
        }

        public ClassUnderTestSimple getInnerObject() {
            return innerObject;
        }
    }

    private static class ClassUnderTestSimple {
        private Integer variable;

        public ClassUnderTestSimple() {
        }

        public ClassUnderTestSimple(Integer variable) {
            this.variable = variable;
        }

        public Integer getVariable() {
            return variable;
        }
    }

    private static class ClassUnderTestWithNoNullConstructorInnerObject {
        private ClassUnderTestWithNoNullConstructor innerObject;

        public ClassUnderTestWithNoNullConstructorInnerObject() {
        }

        public ClassUnderTestWithNoNullConstructorInnerObject(ClassUnderTestWithNoNullConstructor innerObject) {
            this.innerObject = innerObject;
        }

        public ClassUnderTestWithNoNullConstructor getInnerObject() {
            return innerObject;
        }
    }

    @SuppressWarnings("unused")
    private static class ClassUnderTestWithNoNullConstructor {
        public ClassUnderTestWithNoNullConstructor(Object o) {
        }
    }
}
