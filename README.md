#Fluent Input Validator

Easy way to validate complex objects.

###Sample usage
```java
    validate(myObject).withDefaultName()
                      .given(MyClass::getInnerIterableObject)
                      .validateInternals(v -> v.given(MyIterableElement::getVariable)
                                               .expectThat(isNotNull()))
                                               .when(isNumeric())
                                               .expectThat(isInRangeExclusive(2, 7))
                      .and()
                      .given(MyClass::getInnerSimpleObject)
                      .expectThat(isNotNull())
                      .ifErrorsPresent()
                      .throwValidationException();
```

####Credits
Uses [Benji Weber's method reference name resolving tools][].

[Benji Weber's method reference name resolving tools]: https://github.com/benjiman/benjiql/tree/master/src/main/java/uk/co/benjiweber/benjiql/mocking

