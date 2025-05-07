package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.bindAnnotatedFields;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class TestDataCollectionProxyTest {

    interface NotACollection {}

    class ClassThatInjectsNonCollection {
        @TestData
        NotACollection notACollection;
    }

    @Test
    void cannotInjectTestDataAtAllForInterfaceWithNoAnnotation() {
        var loader = new TestDataLoader();
        var object = new ClassThatInjectsNonCollection();

        assertThatThrownBy(() -> bindAnnotatedFields(loader, object)).isInstanceOf(IOException.class);
    }

    @TestDataCollection("child")
    interface ChildCollection {
        @TestData("somefile.txt")
        String someText();

        String someOtherMethod();
    }

    class InjectNestedCollection {
        @TestData("loader")
        ChildCollection childCollection;
    }

    @Test
    void canLoadChildViaPaths() throws Exception {
        var loader = new TestDataLoader();
        var object = new InjectNestedCollection();

        bindAnnotatedFields(loader, object);

        assertThat(object.childCollection.someText()).isEqualTo("Child1\nChild2");
    }

    @Test
    void nonTestDataMethodsReturnNull() throws Exception {
        var loader = new TestDataLoader();
        var object = new InjectNestedCollection();

        bindAnnotatedFields(loader, object);

        assertThat(object.childCollection.someOtherMethod()).isNull();
    }
}
