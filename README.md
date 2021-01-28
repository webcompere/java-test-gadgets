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
// construct the retryer
retryer()
    .retry(() -> {
        // test code that might fail
    });
```

The number of iterations can be set with `times` and the amount of time to wait between is set with `waitBetween`. The code under test can be `void` or return a value:

```java
String result = retryer()
    .times(10)
    .waitBetween(Duration.ofSeconds(1))
    .retry(() -> callThingThatReturnsResult());
```

A common use case for this would be to wrap an assertion with the retryer while waiting
for an asynchronous state to change. E.g.:

```java
AppClient clientToRunningApp = ...;
retryer()
   .retry(() -> assertThat(clientToRunningApp.getCompletedJobs())
          .isEqualTo(10));
```

The configuration of the retryer can be shared across multiple tests:

```java
private static final RETRY_FOR_10_SECONDS = retryer()
    .times(10)
    .waitBetween(Duration.ofSeconds(1));

@Test
void someTest() {
    RETRY_FOR_10_SECONDS.retry(() -> doSomething());
}
```

Retries are also possible via a **JUnit Rule** from the **JUnit4** module:

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

## Test Plugins via the `TestWrapper` Runner

Sometimes we wish to take actions before the test runner gets a chance to inspect the test class. For example, we may wish to dynamically generate some resources, or decide NOT to run the test at all.

Use cases:

- Filter out an entire test class so none of its JUnit actions occur
- Prepare some test data for a data driven test runner - example, prepare the `.feature` files for a Cucumber test before the `Cucumber` runner gets to inspect the target directory
- Perform some tidy up after the entire test suite has run, outside of the `@AfterClass` lifecycle

To do this, change the runner of the test to `TestWrapper`

```java
@RunWith(TestWrapper.class)
public class SomeTest {
  
}
```

Then you can add plugins as `public static` fields, and the plugins will be executed by the `TestWrapper` in a lifecycle surrounding the actual test runner. 

There are three types of plugin, each of which is annotated with `@Plugin`.

```java
@Plugin
public static TestFilter testFilter = ...;

@Plugin
public static BeforeAction beforeAction = ...;

@Plugin
public static AfterAction afterAction = ...;
```

There can be any number of these plugins, and a plugin may implement multiple of the interfaces. The method on the interface is passed the test class's type, so a general purpose plugin can be written that uses the annotations/metadata of the test class to do its job. See `CategoryFilter` in the next section for an example.

### Which Runner Runs the Test?

The `TestWrapper` creates a parent test with the tests run by a child runner inside. This is like using the `Enclosed` runner, but without having to specifically declare an inner test. The default delegates to the standard JUnit `BlockJUnit4Runner` so all test features inside a simple `TestWrapper` run class will work normally. 

However, the `WrapperOptions` annotation can be added to choose a different runner:

```java
@RunWith(TestWrapper.class)
@WrapperOptions(runnerClass = SpringJUnit4ClassRunner.class)
... spring annotations ...
public class SomeSpringTest {
  
}
```

### Comparison with `@BeforeClass` and `@Rule`

If the native features of a JUnit class can be used, then they should be. 

There are times, however, when it's necessary to do something before the test runner you are using has a chance to execute its default behaviour. These plugins take control before the real runner gets to do something. This part of the lifecycle is impossible to control any other way.

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

## Category Filter

The natural consequence of having `@Category` and the `@Plugin` framework provided by `TestWrapper` is that it's possible to put a test category on a whole test class and stop that class even being considered for any form of test execution when the category criteria are not met.

This can be quite important for tests where the static setup of the test is quite a lot of effort but the current run does not require any of the tests to run at all. Normally a test class would execute its fixture-level setup before executing any rules that might turn off individual test methods.

The category filter can be achieved by using the `TestWrapper` along with the test runner that was intended. Let's imagine it was going to be the `Cucumber` runner. All we need to add is the `@Plugin` and the `CategoryFilter` to observe the `@Category` tag on the whole test:

```java
@RunWith(TestWrapper.class)
@Wrapperptions(runnerClass = Cucumber.class)
@CucumberOptions(...)
@Category("IntegrationEnvironmentOnly")
public class CucumberIntegationTests {
    // stop the test from running according to the category annotation 
    // and current environment variables
    @Plugin
    public static CategoryFilter catgeoryFilter = new CategoryFilter();
}
```

## Dependent Tests

People migrating from TestNG to JUnit may miss the features of TestNG that support test ordering, in particular:

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

## Parallel Test Execution

**Available in JUnit 4**, the `ParallelEnclosedRunner` is an extension of the JUnit `Enclosed` runner. The JUnit team allowed for the possibility of multi-threaded tests in their original design, and this runner uses the mechanisms available.

Example:

```java
 @RunWith(ParallelEnclosedRunner.class)
 public class MyTest {
     public static class TestClass1 {
         // tests in here run linearly, but the other
         // test class also runs in parallel
         @Test
         public void test1() {
         }
       
         // other tests
     }
 
     // allows the child tests to have their own test runners
     @RunWith(SomeOtherRunner.class)
     public static class TestClass2 {
        // test in here run in parallel with tests from the other class
        @Test
        public void test2() {
        }
    }
 }
```

Each child of the test class is an independent test class, which can have its own runner, configurations etc. Like the JUnit `Enclosed` runner, all child class tests are executed within the parent class. However, this runner uses a threadpool to execute the children classes in parallel. A child class can also use the `ParallelEnclosedRunner` with its own thread pool to run other child tests in parallel.

The number of threads can be defined by adding a `ParallelOptions` annotation:

```java
@RunWith(ParallelEnclosedRunner.class)
@ParallelOptions(poolSize=99)
public class MyTest {
   // ...
}
```

It may be desirable to increase this number if there are more child test classes and they are ALL to run in parallel, or to reduce the number to throttle the maximum number of child tests that can run at the same time.

This runner can help with integration tests where there are multiple slow-running independent operations to be executed on different parts of the system.

This could technically be used to create a hierarchy of parallel running tests, though the failure of one test won't affect any of the others.

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
