package validator;

/**
 * Used to validate complex fields with {@link FluentInputValidator}.
 *
 * @author Paweł Fiuk
 * @see FluentInputValidator.FieldValidator#validateUsing(SpecializedValidator)
 */

public interface SpecializedValidator<T> {
    ValidationMap getValidationFor(T input, String inputName);
}
