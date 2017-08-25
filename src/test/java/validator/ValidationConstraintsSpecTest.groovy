package validator

import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ValidationException

import static validator.FluentInputValidator.validate
import static validator.ValidationConstraints.*

class ValidationConstraintsSpecTest extends Specification {

    @Unroll("Should throw ValidationException for field #field for given constraint #constraint")
    "Should validate input for given constraint and throw ValidationException"() {
        given:
        def baseInput = new TestInput(field: input)

        when:
        validate(baseInput).as("input")
                           .given(baseInput.getField(), "field")
                           .expectThat(constraint)
                           .ifErrorsPresent()
                           .throwValidationException()

        then:
        ValidationException exception = thrown()
        exception.message.contains "input.field"

        where:
        input             | constraint
        "a"               | isEqualTo("b")
        null              | isNotNull()
        ""                | isNotEmpty()
        "   "             | isNotWhitespace()
        "abc"             | isWhitespace()
        "  "              | isNotBlank()
        "abc"             | isBlank()
        "12"              | isLongerOrEqualTo(3)
        "123"             | isLongerThan(3)
        "1234"            | isShorterOrEqualTo(3)
        "123"             | isShorterThan(3)
        "12"              | hasLengthEqualTo(3)
        3L                | isInRangeExclusive(3L, 5L)
        3.2               | isInRangeExclusive(3.2, 5.0)
        2L                | isInRangeInclusive(3L, 5L)
        3.1               | isInRangeInclusive(3.2, 5.0)
        "abc"             | isNumeric()
        "123..4"          | isDouble()
        TestEnumB.VALUE_C | isValidAsEnum(TestEnumA.class)
    }

    @Unroll("Should not throw any exceptions for field #field for given constraint #constraint")
    "Should validate input for given constraint and not throw any exceptions"() {
        given:
        def baseInput = new TestInput(field: input)

        when:
        validate(baseInput).as("input")
                           .given(baseInput.getField(), "field")
                           .expectThat(constraint)
                           .ifErrorsPresent()
                           .throwValidationException()

        then:
        noExceptionThrown()

        where:
        input             | constraint
        "a"               | isEqualTo("a")
        ""                | isNotNull()
        " "               | isNotEmpty()
        "abc"             | isNotWhitespace()
        "   "             | isWhitespace()
        "abc"             | isNotBlank()
        null              | isBlank()
        "123"             | isLongerOrEqualTo(3)
        "1234"            | isLongerThan(3)
        "123"             | isShorterOrEqualTo(3)
        "12"              | isShorterThan(3)
        "123"             | hasLengthEqualTo(3)
        4L                | isInRangeExclusive(3L, 5L)
        4.1               | isInRangeExclusive(3.2, 5.0)
        3L                | isInRangeInclusive(3L, 5L)
        3.3               | isInRangeInclusive(3.2, 5.0)
        "123"             | isNumeric()
        "123.4"           | isDouble()
        TestEnumB.VALUE_A | isValidAsEnum(TestEnumA.class)
    }

    private static class TestInput {
        private Object field

        Object getField() {
            return field
        }
    }

    private static enum TestEnumA {
        VALUE_A,
        VALUE_B
    }

    private static enum TestEnumB {
        VALUE_A,
        VALUE_C
    }

}
