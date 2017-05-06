# Fluent Input Validator

Easy way to validate complex objects.

Outputs a validation map with all collected errors.

## Table of contents

* [Sample usage](#sample-usage)
 * [Output in JSON format](#output-in-json-format)
* [Credits](#credits)

## Sample usage

```java
validate(myObject).withDefaultName()
                  .given(MyObject::getInnerComplexObject)
                  .validateInternals(v -> v.given(MyInnerComplexObject::getVariable)
                                           .expectThat(isNotNull()))
                                           .when(isNumeric())
                                           .expectThat(isInRangeExclusive(2, 7))
                  .and()
                  .given(MyObject::getInnerSimpleObject)
                  .expectThat(isNotNull(),
                              isNotEmpty(),
                              isNotWhitespace())
                  .ifErrorsPresent()
                  .throwValidationException();
```

### Output in JSON format

```json
{
    "MyObject.innerComplexObject.variable": [
        "may not be null"
    ],
    "MyObject.innerSimpleObject": [
        "may not be empty",
        "may not be whitespace"
    ]
}
```

## Credits

Uses [Benji Weber's method reference name resolving tools][].

[Benji Weber's method reference name resolving tools]: https://github.com/benjiman/benjiql/tree/master/src/main/java/uk/co/benjiweber/benjiql/mocking
