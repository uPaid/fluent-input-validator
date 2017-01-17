#Fluent Input Validator

Easy way to validate complex objects.

###Sample usage

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
                      .getValidationResults();

