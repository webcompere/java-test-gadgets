package uk.org.webcompere.testgadgets;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static uk.org.webcompere.testgadgets.TestResource.with;

@ExtendWith(MockitoExtension.class)
class TestResourceTest {
    private static int someNumber;

    @Mock
    private TestResource mock1;

    @Mock
    private TestResource mock2;

    @Mock
    private TestResource mock3;

    @Test
    void testResourceExecuteAroundIdiom() throws Exception {
        TestResource resource = new TestResource() {
            @Override
            public void setup() throws Exception {
                someNumber++;
            }

            @Override
            public void teardown() throws Exception {
                someNumber--;
            }
        };

        // the resource is not set up
        assertThat(someNumber).isZero();
        resource.execute(() -> {
            assertThat(someNumber).isEqualTo(1);
        });
        // outside execute the resource is released
        assertThat(someNumber).isZero();

        // and we can also extract results from within execution
        int result = resource.execute(() -> someNumber);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void multipleResources() throws Exception {
        TestResource resource1 = new TestResource() {
            @Override
            public void setup() throws Exception {
                someNumber++;
            }

            @Override
            public void teardown() throws Exception {
                someNumber--;
            }
        };

        TestResource resource2 = new TestResource() {
            @Override
            public void setup() throws Exception {
                someNumber *= 10;
            }

            @Override
            public void teardown() throws Exception {
                someNumber /= 10;
            }
        };

        with(resource1, resource2)
            .execute(() -> assertThat(someNumber).isEqualTo(10));

        assertThat(someNumber).isZero();
    }

    @Test
    void multipleResourcesAllSetupAndTornDown() throws Exception {
        with(mock1, mock2, mock3)
            .execute(() -> {});

        then(mock1)
            .should()
            .setup();

        then(mock2)
            .should()
            .setup();

        then(mock3)
            .should()
            .setup();

        then(mock1)
            .should()
            .teardown();

        then(mock2)
            .should()
            .teardown();

        then(mock3)
            .should()
            .teardown();
    }

    @Test
    void whenAResourceFailsToSetUpItsTeardownAndSubsequentAreCancelled() throws Exception {
        willThrow(new RuntimeException("bang"))
            .given(mock2)
            .setup();

        assertThatThrownBy(() -> with(mock1, mock2, mock3)
            .execute(() -> {}))
            .isInstanceOf(RuntimeException.class);

        then(mock1)
            .should()
            .setup();

        then(mock2)
            .should()
            .setup();

        then(mock3)
            .should(never())
            .setup();

        then(mock1)
            .should()
            .teardown();

        then(mock2)
            .should(never())
            .teardown();

        then(mock3)
            .should(never())
            .teardown();
    }
}
