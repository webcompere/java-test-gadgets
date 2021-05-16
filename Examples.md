# Example Use Cases

## WireMock JUnit 5 Extension

The `WireMock` JUnit rules allow for a shared instance of WireMock across
multiple tests (with the ClassRule) or one instance per test.

While there is a third party JUnit 5 extension - a subclass of the original
WireMock code - the `DangerousRuleAdapter` is safe to use as a JUnit 5
plugin for WireMock:

```java
@ExtendWith(PluginExtension.class)
@Nested
class WireMockExtensionExampleTest {
    @Plugin
    private DangerousRuleAdapter<WireMockRule> wireMock =
        new DangerousRuleAdapter<>(new WireMockRule(Options.DYNAMIC_PORT));

    private WireMockServer wireMockServer = wireMock.get();

    @Test
    void serverIsUp() {
        assertThat(wireMockServer.isRunning()).isTrue();
    }
}
```

In this example the wiremock code is wrapped with the adapter, and
marked `@Plugin`. The `PluginExtension` takes care of its lifecycle. If
the field were `static`, then it would be built once for the whole
test suite, and in the above case, it's built once per test method.
