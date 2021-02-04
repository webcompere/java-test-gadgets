package uk.org.webcompere.testgadgets.plugin;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.webcompere.testgadgets.TestResource;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReferenceCountingTestResourceTest {
    @Mock
    private TestResource decoratee;

    @InjectMocks
    private ReferenceCountingTestResource resource;

    @Test
    void whenSetupThenDecorateeSetup() throws Exception {
        resource.setup();

        then(decoratee)
            .should()
            .setup();
    }

    @Test
    void whenTwoSetupsThenDecorateeSetupOnce() throws Exception {
        resource.setup();
        resource.setup();

        then(decoratee)
            .should()
            .setup();
    }

    @Test
    void whenTwoSetupsAndOneTeardownThenDecorateeNotTornDown() throws Exception {
        resource.setup();
        resource.setup();

        then(decoratee)
            .should()
            .setup();

        then(decoratee)
            .should(never())
            .teardown();
    }

    @Test
    void whenPairOfSetupTeardownThenBothCalled() throws Exception {
        resource.setup();
        resource.teardown();

        then(decoratee)
            .should()
            .setup();

        then(decoratee)
            .should()
            .teardown();
    }

    @Test
    void whenDoublePairOfSetupTeardownThenBothCalled() throws Exception {
        resource.setup();
        resource.setup();
        resource.teardown();
        resource.teardown();

        then(decoratee)
            .should()
            .setup();

        then(decoratee)
            .should()
            .teardown();
    }

    @Test
    void whenSequentialDoublePairOfSetupTeardownThenBothCalled() throws Exception {
        resource.setup();
        resource.teardown();
        resource.setup();
        resource.teardown();

        then(decoratee)
            .should(times(2))
            .setup();

        then(decoratee)
            .should(times(2))
            .teardown();
    }

    @Test
    void prematureTeardownFollowedBySetupTearDownDoesOneSetupTeardown() throws Exception {
        resource.teardown();
        resource.setup();
        resource.teardown();

        then(decoratee)
            .should()
            .setup();

        then(decoratee)
            .should()
            .teardown();
    }
}
