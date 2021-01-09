# Java Test Gadgets
An assortment of testing tools/tricks for JUnit in Java

[![Build status](https://ci.appveyor.com/api/projects/status/my25e52wqat798qu?svg=true)](https://ci.appveyor.com/project/ashleyfrieze/java-test-gadgets)
[![codecov](https://codecov.io/gh/webcompere/java-test-gadgets/branch/main/graph/badge.svg?token=5VV2KJX5KH)](https://codecov.io/gh/webcompere/java-test-gadgets)

- `java-test-gadgets-core` - tools that can be used in any test framework
- `java-test-gagdets-junit4` - custom plugins for JUnit 4
- `java-test-gadgets-jupiter` - custom plugins for JUnit 5

This library contains a few tools to help with TDD and Unit tests. They are largely
unrelated, and have come out of solving real-world problems.

## Installation

_tbc_ will be on Maven Central

## Gadgets

### Retries

The `Retryer` class in **TestGadgets Core** allows code to be wrapped with retry logic for testing:

```java
retry(() -> {
  // test code that might fail
    }, repeat().times(3));
```

The `repeat` function will generate a default set of retries. The number of iterations can be set with `times` and the amout of time to wait between is set with `waitBetween`. The code under test can be `void` or return a value:

```java
String result = retry(() -> callThingThatReturnsResult(),
                     repeat().times(10).waitBetween(Duration.ofSeconds(1)));
```

This can be used via a **JUnit Rule** from the **JUnit4** module:

```java
@Rule
public RetryTests retryTests = new RetryTests(2, Duration.ofMillis(2));
```

The rule will retry all tests, though it will only retry the test method, not the `@Before` or `@After` etc. If a test should not be retried than that test can be annotated with `@DoNotRetry` to prevent the rule executing on it:

```java
@Test
public void someTest() {
  // would be retried up to the limit of the RetryTests rule
}

@DoNotRetry
public void nonRetried() {
  // fails immediately without retries
}
```

Note: if the tests change the state of the test object, then allowing them to retry may cause unexpected side effects.

## Contributing

If you have any issues or improvements, please
[at least submit an issue](https://github.com/webcompere/java-test-gadgets/issues).

Please also feel free to fork the project and submit a PR for consideration.

* Please write a test for your change
* Ensure that the build succeeds and there's no drop in code coverage - see the GitHub PR for feedback

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.
