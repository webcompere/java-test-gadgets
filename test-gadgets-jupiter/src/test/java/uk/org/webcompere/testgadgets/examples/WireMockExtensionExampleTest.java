package uk.org.webcompere.testgadgets.examples;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.testgadgets.plugin.Plugin;
import uk.org.webcompere.testgadgets.plugin.PluginExtension;
import uk.org.webcompere.testgadgets.rules.DangerousRuleAdapter;

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
