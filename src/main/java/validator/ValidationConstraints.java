package validator;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.Enum.valueOf;
import static java.util.Objects.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Collection of static methods that can be used in {@link FluentInputValidator}.
 * They are meant to be imported statically.
 *
 * @author Paweł Fiuk
 * TODO: provide resource file with validation messages
 */

public class ValidationConstraints {

    private ValidationConstraints() {
    }

    public interface ValidationConstraint {
        /**
         * @param object validated object
         *
         * @return validation error message or null if object is valid
         */
        String getErrorFor(Object object);
    }

    private static final ValidationConstraint ALWAYS_VALID = object -> null;

    /**
     * Checks if validated object is equal to given object.
     */
    public static ValidationConstraint isEqualTo(Object other) {
        return object -> {
            if (!Objects.equals(object, other)) {
                return "must be equal to " + other;
            }
            return null;
        };
    }

    /**
     * Checks if validated object is not null.
     */
    public static ValidationConstraint isNotNull() {
        return object -> {
            if (isNull(object)) {
                return "may not be null";
            }
            return null;
        };
    }

    /**
     * Checks if validated collection or {@link String} representation of object is not empty.
     *
     * @see StringUtils#isEmpty(CharSequence)
     */
    public static ValidationConstraint isNotEmpty() {
        return object -> {
            if (object instanceof Collection<?> && ((Collection<?>) object).isEmpty()) {
                return "may not be empty";
            }
            else if (isEmpty(asString(object))) {
                return "may not be empty";
            }
            return null;
        };
    }

    /**
     * Checks if validated collection or {@link String} representation of object is not whitespace.
     *
     * @see StringUtils#isWhitespace(CharSequence)
     */
    public static ValidationConstraint isNotWhitespace() {
        return object -> {
            if (StringUtils.isWhitespace(asString(object))) {
                return "may not be whitespace";
            }
            return null;
        };
    }

    /**
     * Checks if validated collection or {@link String} representation of object is not whitespace.
     *
     * @see StringUtils#isWhitespace(CharSequence)
     */
    public static ValidationConstraint isWhitespace() {
        return object -> {
            if (!StringUtils.isWhitespace(asString(object))) {
                return "may only be whitespace";
            }
            return null;
        };
    }

    /**
     * Checks if validated collection or {@link String} representation of object is not blank.
     *
     * @see StringUtils#isNotBlank(CharSequence)
     */
    public static ValidationConstraint isNotBlank() {
        return object -> {
            if (StringUtils.isBlank(asString(object))) {
                return "may not be blank";
            }
            return null;
        };
    }

    /**
     * Checks if validated collection or {@link String} representation of object is blank.
     *
     * @see StringUtils#isBlank(CharSequence)
     */
    public static ValidationConstraint isBlank() {
        return object -> {
            if (!StringUtils.isBlank(asString(object))) {
                return "may only be blank";
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation is longer than given length.
     *
     * @see #isLongerOrEqualTo(long)
     */
    public static ValidationConstraint isLongerThan(long minimalLength) {
        return isLongerOrEqualTo(minimalLength + 1);
    }

    /**
     * Checks if validated object's {@link String} representation is longer or equal to given length.
     *
     * @see #isLongerThan(long)
     */
    public static ValidationConstraint isLongerOrEqualTo(long minimalLength) {
        return minimalLength <= 0 ? ALWAYS_VALID : object -> {
            if (asString(object).length() < minimalLength) {
                return "may not be shorter than " + minimalLength;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation is shorter than given length.
     *
     * @see #isShorterOrEqualTo(long)
     */
    public static ValidationConstraint isShorterThan(long maximalLength) {
        return isShorterOrEqualTo(maximalLength - 1);
    }

    /**
     * Checks if validated object's {@link String} representation is shorter or equal to given length.
     *
     * @see #isShorterThan(long)
     */
    public static ValidationConstraint isShorterOrEqualTo(long maximalLength) {
        return object -> {
            if (asString(object).length() > maximalLength) {
                return "may not be longer than " + maximalLength;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation's length is equal to given length.
     */
    public static ValidationConstraint hasLengthEqualTo(long expectedLength) {
        return object -> {
            if (asString(object).length() != expectedLength) {
                return "may not be longer or shorter than " + expectedLength;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link Long} representation is in given range.
     *
     * @see #isInRangeExclusive(double, double)
     * @see #isInRangeInclusive(long, long)
     * @see #isInRangeInclusive(double, double)
     */
    public static ValidationConstraint isInRangeExclusive(long min, long max) {
        return object -> {
            if (asLong(object) >= max || asLong(object) <= min) {
                return "value must be between " + min + " and " + max;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link Double} representation is in given range.
     *
     * @see #isInRangeExclusive(long, long)
     * @see #isInRangeInclusive(long, long)
     * @see #isInRangeInclusive(double, double)
     */
    public static ValidationConstraint isInRangeExclusive(double min, double max) {
        return object -> {
            if (asDouble(object) >= max || asDouble(object) <= min) {
                return "value must be between " + min + " and " + max;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link Long} representation is in given range.
     *
     * @see #isInRangeExclusive(long, long)
     * @see #isInRangeExclusive(double, double)
     * @see #isInRangeInclusive(double, double)
     */
    public static ValidationConstraint isInRangeInclusive(long min, long max) {
        return object -> {
            if (asLong(object) > max || asLong(object) < min) {
                return "value must be between " + min + " and " + max;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link Double} representation is in given range.
     *
     * @see #isInRangeExclusive(long, long)
     * @see #isInRangeExclusive(double, double)
     * @see #isInRangeInclusive(long, long)
     */
    public static ValidationConstraint isInRangeInclusive(double min, double max) {
        return object -> {
            if (asDouble(object) > max || asDouble(object) < min) {
                return "value must be between " + min + " and " + max;
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation consists of digits and periods.
     *
     * @see StringUtils#isNumeric(CharSequence)
     */
    public static ValidationConstraint isNumeric() {
        return object -> {
            if (!StringUtils.isNumeric(asString(object))) {
                return "must be numeric";
            }
            return null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation can be parsed to {@link Double} value.
     */
    public static ValidationConstraint isDouble() {
        return object -> {
            String error = isMatchingPattern("[0-9]+.?[0-9]*").getErrorFor(object);
            return nonNull(error) ? "must be a floating point number" : null;
        };
    }

    /**
     * Checks if validated object's {@link String} representation matches given pattern.
     */
    public static ValidationConstraint isMatchingPattern(String pattern) {
        return object -> {
            if (!asString(object).matches(pattern)) {
                return "does not match pattern";
            }
            return null;
        };
    }

    /**
     * Checks if field is an instance of {@link Enum} and have corresponding value in given enum class.
     */
    public static ValidationConstraint isValidAsEnum(Class<? extends Enum> expectedClass) {
        return object -> {
            try {
                valueOf(expectedClass, ((Enum) object).name());
            } catch (Exception ex) {
                return "invalid field value";
            }
            return null;
        };
    }

    /**
     * Checks if testing the field against predicate returns true.
     */
    public static ValidationConstraint fulfills(Predicate<? super Object> predicate) {
        return object -> {
            try {
                return predicate.test(object) ? null : "does not fulfill predicate";
            } catch (Exception ex) {
                return "exception thrown while testing against predicate";
            }
        };
    }

    private static Double asDouble(Object object) {
        if (object instanceof Double) {
            return (Double) object;
        }
        else {
            return Double.valueOf(asString(object));
        }
    }

    private static Integer asLong(Object object) {
        if (object instanceof Integer) {
            return (Integer) object;
        }
        else {
            return Integer.valueOf(asString(object));
        }
    }

    private static String asString(Object object) {
        if (isNull(object)) {
            return "";
        }
        return object.toString();
    }
}
