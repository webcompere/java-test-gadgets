package uk.org.webcompere.testgadgets.junit4.order;

import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import uk.org.webcompere.testgadgets.junit4.category.Category;
import uk.org.webcompere.testgadgets.junit4.category.CategoryRule;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assume.assumeTrue;

/**
 * Add this to the definition of a test class with <code>@RunWith(DependentTestRunner.class)</code> in order to
 * get two features from TestNG into a JUnit test.<br>
 * <ul>
 *     <li>Execution order of tests - set by applying a <code>@Priority(1)</code> etc on the test method</li>
 *     <li>Dependency between tests - also affects the order, but also affects whether a dependent test runs
 *     when one of its dependencies fails</li>
 * </ul>
 * <br>
 * The ordering algorithm pays attention to the presence of {@link org.junit.FixMethodOrder}, then filters by
 * priority, then filters by dependency.<br>
 * Tests can depend on several tests. Cyclic dependencies are not allowed. Failure of an early test will be an
 * assumption failure of a dependent test. Watch out for expensive <code>Before</code> and <code>After</code> code
 * in this instance.<br>
 * Any test that is depended upon, is higher priority than any non depended upon test. Priority order within tests
 * that are depended upon is also respected.<br>
 * This test automatically supports {@link CategoryRule} and allows the test class to have the {@link Category}
 * annotation as an override on top of per method categories.
 * @see DependOnPassing
 * @see Priority
 * @see Category
 * @see CategoryRule
 */
public class DependentTestRunner extends BlockJUnit4ClassRunner {
    private CategoryRule categoryRule = new CategoryRule();

    /**
     * Forces dependent methods to fail if their dependency has failed
     */
    private class DependencyStatement extends Statement {
        private FrameworkMethod method;
        private Statement base;

        public DependencyStatement(FrameworkMethod method, Statement base) {
            this.method = method;
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                dependencies.get(method)
                        .forEach(dependentTest ->
                                assumeTrue("Dependent method: " + dependentTest.getName() + " failed",
                                        !hasFailed.contains(dependentTest)));

                base.evaluate();
            } catch (Throwable t) {
                hasFailed.add(method);
                throw t;
            }
        }
    }

    /**
     * Plugs in the dependency statement using JUnit rules.
     */
    private class DependencyRule implements MethodRule {
        @Override
        public Statement apply(Statement base, FrameworkMethod method, Object target) {
            return new DependencyStatement(method, base);
        }
    }

    private Set<FrameworkMethod> hasFailed = new HashSet<>();
    private Map<FrameworkMethod, List<FrameworkMethod>> dependencies;
    private Map<String, FrameworkMethod> methodMap;
    private List<FrameworkMethod> children;

    private DependencyRule dependencyRule = new DependencyRule();

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass the type of test class being executed
     * @throws InitializationError if the test class is malformed.
     */
    public DependentTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

        List<FrameworkMethod> childrenInPriorityOrder = prioritise(filterForCategory(super.getChildren()));

        methodMap = childrenInPriorityOrder.stream()
                .collect(toMap(FrameworkMethod::getName, Function.identity()));

        calculateDependencies(childrenInPriorityOrder);
        checkForCyclicDependencies(childrenInPriorityOrder);

        children = promoteDependencies(childrenInPriorityOrder);
    }

    private List<FrameworkMethod> filterForCategory(List<FrameworkMethod> children) {
        if (!categoryRule.isPermitted(getTestClass().getAnnotation(Category.class))) {
            return emptyList();
        }

        return children.stream()
                .filter(categoryRule::isPermitted)
                .collect(toList());
    }

    private List<FrameworkMethod> prioritise(List<FrameworkMethod> children) {
        List<FrameworkMethod> unprioritised = new ArrayList<>();
        List<FrameworkMethod> withPriority = new ArrayList<>();
        for (FrameworkMethod child : children) {
            if (child.getAnnotation(Priority.class) != null) {
                withPriority.add(child);
            } else {
                unprioritised.add(child);
            }
        }

        return Stream.concat(sorted(withPriority), unprioritised.stream()).collect(toList());
    }

    private Stream<FrameworkMethod> sorted(List<FrameworkMethod> sortByPriority) {
        return sortByPriority.stream()
                .sorted(Comparator.comparingInt(method -> method.getAnnotation(Priority.class).value()));
    }

    private void calculateDependencies(List<FrameworkMethod> childrenInOriginalOrder) throws InitializationError {
        dependencies = new HashMap<>();
        for (FrameworkMethod method:childrenInOriginalOrder) {
            dependencies.put(method, getDependencies(method));
        }
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return children;
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        return Stream.concat(super.rules(target).stream(),
                Stream.of(dependencyRule)).collect(toList());
    }

    private List<FrameworkMethod> promoteDependencies(List<FrameworkMethod> unpromotedMethods) {
        List<FrameworkMethod> notDependedOn = new ArrayList<>();
        List<FrameworkMethod> dependedOn = new ArrayList<>();

        categoriseByDependency(unpromotedMethods, notDependedOn, dependedOn);

        if (dependedOn.isEmpty()) {
            return notDependedOn;
        }

        // recurse and put the ones we found not to be dependend on as lower
        return Stream.concat(promoteDependencies(dependedOn).stream(), notDependedOn.stream())
                .collect(toList());
    }

    private void categoriseByDependency(List<FrameworkMethod> methods, List<FrameworkMethod> notDependedOn,
                                        List<FrameworkMethod> dependedOn) {
        for (FrameworkMethod method:methods) {
            if (isDependedOn(method, methods)) {
                dependedOn.add(method);
            } else {
                notDependedOn.add(method);
            }
        }
    }

    private boolean isDependedOn(FrameworkMethod possibleDependency, List<FrameworkMethod> methods) {
        return methods.stream()
                .anyMatch(method -> method != possibleDependency && methodDependsOn(method, possibleDependency));
    }

    private boolean methodDependsOn(FrameworkMethod method, FrameworkMethod possibleDependency) {
        List<FrameworkMethod> dependentMethods = dependencies.get(method);
        if (dependentMethods == null) {
            return false;
        }
        return dependentMethods.contains(possibleDependency);
    }


    private void checkForCyclicDependencies(List<FrameworkMethod> methods) throws InitializationError {
        for (FrameworkMethod method:methods) {
            Set<FrameworkMethod> dependenciesSoFar = new HashSet<>();
            proveNoRepeatedDependencies(method, dependenciesSoFar, method);
        }
    }

    private void proveNoRepeatedDependencies(FrameworkMethod originalMethod, Set<FrameworkMethod> dependenciesSoFar,
                                             FrameworkMethod currentMethod) throws InitializationError {
        if (dependenciesSoFar.contains(currentMethod)) {
            throw new InitializationError("Cyclic dependency: " +
                originalMethod.getName() + " -> " + dependenciesSoFar);
        }
        dependenciesSoFar.add(currentMethod);
        for (FrameworkMethod dependency:dependencies.get(currentMethod)) {
            proveNoRepeatedDependencies(originalMethod, dependenciesSoFar, dependency);
        }
    }

    private List<FrameworkMethod> getDependencies(FrameworkMethod method) throws InitializationError {
        DependOnPassing annotation = method.getAnnotation(DependOnPassing.class);
        if (annotation == null) {
            return emptyList();
        }

        return convertToMethods(annotation.value());
    }

    private List<FrameworkMethod> convertToMethods(String[] dependencies) throws InitializationError {
        List<FrameworkMethod> result = new ArrayList<>();
        for (String dependency:dependencies) {
            result.add(convertToMethod(dependency));
        }
        return result;
    }

    private FrameworkMethod convertToMethod(String name) throws InitializationError {
        FrameworkMethod method = methodMap.get(name);
        if (method == null) {
            throw new InitializationError("Cannot find dependent method " + name);
        }
        return method;
    }
}
