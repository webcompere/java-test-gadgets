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

## Retries

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

## Run JUnit4 `TestRule` outside of JUnit 4

Available in the **core** module, the `Rules` class allows interoperability of JUnit 4 `TestRule` objects in non JUnit 4 tests. It introduces the execute-around idiom.

**Warning:** this will work with some test rules and not with others. Where a test rule needs to use the annotations on the actual test methods, then this will fail. It is most suitable for use with objects that provide a resource of some sort.

Let's use JUnit4's `TemporaryFolder` rule as an example (even though there's a JUnit5 native alternative you could use).

```java
@Test
void testMethod() throws Exception {
  TemporaryFolder temporaryFolder = new TemporaryFolder();

  // let's use this temp folder with some test code
  executeWithRule(temporaryFolder, () -> {
    // here, the rule is _active_
    callSomethingThatUses(temporaryFolder.getRoot());
  });
  
  // here the rule has cleaned up
}
```

We can also use this pattern to execute multiple rules in sequence. Let's add `EnvironmentVariableRule` from SystemStubs (again, there are better ways of doing this, but it's a simple example).

```java
// use withRules to construct a set of rules to use together
@Test
void testMethod() throws Exception {
  TemporaryFolder temporaryFolder = new TemporaryFolder();
	EnvironmentVariablesRule environment = new EnvironmentVariables("foo", "bar");
  
  // let's use this temp folder with some test code
  withRules(temporaryFolder, environment)
    .execute(() -> {
    // here, the rules is _active_
    callSomethingThatUses(temporaryFolder.getRoot());
  });
  
  // here the rules have been cleaned up
}
```

The function passed to `executeWithRule` and `execute` can be `void` or can return a value. 

## Test Categories

Goal: Dependent on environment variables, selectively disable/enable individual test cases according to a tag. This is intended for use in CI/CD pipelines where some tests may not be possible in some environments.

Note: This is exclusively available in **JUnit4**. It can be combined with the **Dependent Tests** feature as that feature also uses this rule to apply categories.

To use, first annotate the test with the `CategoryRule` and `@Category` annotations on tests to run selectively:

```java
@Rule
public CategoryRule categoryRule = new CategoryRule();

@Test
public void always() {
}

@Category("cat1")
@Test
public void hasOneCategory() {
}

@Category({"cat1","cat2"})
@Test
public void hasTwoCategories() {
}
```

In the above, the `always` test is unaffected by category selection. The other two tests will run if their categories are within the list of active categories in an environment variable.

Then, set the environment variable `INCLUDE_TEST_CATEGORIES` to a comma separated list of test categories that you wish to include in the test run before executing the test.

### Excluding Categories

Where some tests are in a category that is usually included, it may be necessary to exclude a subset of that. To do this, add another category for the test and then set the `EXCLUDE_TEST_CATEGORIES` environment to a comma separated list of the categories to further exclude.

E.g.

```java
@Rule
public CategoryRule categoryRule = new CategoryRule();

@Category({"integration"})
@Test
public void intTest1() {
  
}

@Category({"integration", "slow"})
@Test
public void rareIntTest() {
  
}

INCLUDE_TEST_CATEGORIES = integration
EXCLUDE_TEST_CATEGORIES = slow  
```

The above would then include the first test because it's in `integration` but would subtract the second because it has `slow`.

### Inverting the Tag

The `@Category` annotation has a field `will` which defaults to `INCLUDE` and causes the behaviour above. However, we may prefer to default to including all tests unless their category is specifically mentioned.

This can be done by setting `will` to `EXCLUDE` and using the `INCLUDE_TEST_CATEGORIES` environment variable.

```java
@Rule
public CategoryRule categoryRule = new CategoryRule();

@Category(value = "cat1", will = EXCLUDE)
@Test
public void notOnCat1() {
}
```

Here, the `notOnCat1` will run unless `cat1` is in the **inclusion list** of the categories.

## Dependent Tests

People migrating from TestNG to JUnit may miss the features of TestNG that support test ordering, in particularly:

- Priority
- One test dependent on the successful completion of another

To achieve these, the `DependentTestRunner` provides both features. The `DependentTestRunner` might otherwise conflict with the `CategoryRule`, so has its own direct integration with the category logic, and thus supports categories.

These tests may best suit integration tests where the dependency or priority is caused by the complex behaviour of the system under test during the test run.

### Priority

```java
@RunWith(DependentTestRunner.class)
public class DependentTestRunnerExampleTest {
    @Test
    @Priority(1)
    public void aTest() {

    }
  
    @Test
    @Priority(2)
    public void lowerPriorityTest() {
      
    }
  
    @Test
    public void noPrioritySoRunAsLowest() {
     
    }
}
```

The `@Priority` annotation describes how important a test is. The lower the number the more urgently the test is run within the fixture. Tests with no `@Priority` are run latest.

### Dependency

Sometimes we need a test to run only after a predecessor. Similarly we may not want a test to be attempted if its predecessor failed. For this we use the `@DependOnPassing` annotation:

```java
@RunWith(DependentTestRunner.class)
public class DependentTestRunnerExampleTest {
    @Test
    @Priority(2)
    @DependOnPassing("anotherTest")
    public void dependentTest() {

    }

    @Test
    public void anotherTest() {

    }
}
```

In this example `anotherTest` MUST be run before `dependentTest` and so runs earlier, even though `dependentTest` technically has a higher priority.

Dependencies can be a tree, and the test runner works out the order based on running the least dependent tests first, executing their dependent as soon as can be allowed.

Binding between tests is by method name, which doesn't tolerate refactoring particularly nicely. However, the test runner first scans all the tests to ensure that the dependencies exist and are not cyclic, before allowing the test to run.

## Contributing

If you have any issues or improvements, please
[at least submit an issue](https://github.com/webcompere/java-test-gadgets/issues).

Please also feel free to fork the project and submit a PR for consideration.

* Please write a test for your change
* Ensure that the build succeeds and there's no drop in code coverage - see the GitHub PR for feedback

The basic coding style is described in the
[EditorConfig](http://editorconfig.org/) file `.editorconfig`.

### Build

```bash
# to build
./mvnw clean install
```

Note: the JUnit4 project includes some test classes which fail on purpose.
These are not built by Maven by default. They may, however, be picked up
by the test runner in an IDE, such as IntelliJ. Test with maven to be sure what
passes or fails.
