package uk.org.webcompere.testgadgets.testdatafactory;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.bindAnnotatedFields;
import static uk.org.webcompere.testgadgets.testdatafactory.TestDataLoaderAnnotations.bindAnnotatedStaticFields;

import java.nio.file.Paths;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class TestDataLoaderAnnotationsTest {
    public static class BoundClass {
        @TestData
        private Catchphrase somejson;

        public Catchphrase getSomejson() {
            return somejson;
        }
    }

    public static class StaticBoundClass {
        @TestData
        private static Catchphrase somejson;

        public static Catchphrase getSomejson() {
            return somejson;
        }
    }

    public static class BoundCachedClass {
        @TestData(immutable = Immutable.IMMUTABLE)
        private Catchphrase somejson;

        public Catchphrase getSomejson() {
            return somejson;
        }
    }

    public static class BoundWithFilename {
        @TestData("somejson.json")
        private Catchphrase catchPhrase;

        public Catchphrase getCatchPhrase() {
            return catchPhrase;
        }
    }

    public static class BoundWithFilePath {
        @TestData({"loader", "somejson.json"})
        private Catchphrase catchPhrase;

        public Catchphrase getCatchPhrase() {
            return catchPhrase;
        }
    }

    public static class BoundWithFilePathSlashed {
        @TestData({"loader/somejson.json"})
        private Catchphrase catchPhrase;

        public Catchphrase getCatchPhrase() {
            return catchPhrase;
        }
    }

    public static class BoundToSupplier {
        @TestData
        private Supplier<Catchphrase> somejson;

        public Supplier<Catchphrase> getSomejson() {
            return somejson;
        }
    }

    @Test
    void canLoadUsingDefaults() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));

        BoundClass bound = new BoundClass();
        bindAnnotatedFields(loader, bound);

        assertThat(bound.getSomejson().getName()).isEqualTo("Gadget");
    }

    @Test
    void whenFilenameInAnnotationThenLoadsIt() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));

        BoundWithFilename bound = new BoundWithFilename();
        bindAnnotatedFields(loader, bound);

        assertThat(bound.getCatchPhrase().getName()).isEqualTo("Gadget");
    }

    @Test
    void whenFilePathInAnnotationThenLoadsIt() throws Exception {
        var loader = new TestDataLoader();

        BoundWithFilePath bound = new BoundWithFilePath();
        bindAnnotatedFields(loader, bound);

        assertThat(bound.getCatchPhrase().getName()).isEqualTo("Gadget");
    }

    @Test
    void whenFilePathInAnnotationWithSlashesThenLoadsIt() throws Exception {
        var loader = new TestDataLoader();

        BoundWithFilePathSlashed bound = new BoundWithFilePathSlashed();
        bindAnnotatedFields(loader, bound);

        assertThat(bound.getCatchPhrase().getName()).isEqualTo("Gadget");
    }

    @Test
    void canLoadWithOverrideType() throws Exception {
        var loader = new TestDataLoader();

        class BoundClass {
            @TestData(
                    value = {"loader", "somejson.json"},
                    as = ".txt")
            String somejson;
        }

        var bound = new BoundClass();
        bindAnnotatedFields(loader, bound);

        // has loaded as string
        assertThat(bound.somejson)
                .isEqualTo("{\n" + "    \"name\": \"Gadget\",\n" + "    \"catchPhrase\": \"GadgetGadget\"\n" + "}");
    }

    @Test
    void whenTheLoaderIsSetToImmutableSubsequentLoadsAreCached() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));
        loader.setImmutableMode(Immutable.IMMUTABLE);

        BoundClass bound = new BoundClass();
        bindAnnotatedFields(loader, bound);

        Catchphrase catch1 = bound.getSomejson();
        bindAnnotatedFields(loader, bound);
        Catchphrase catch2 = bound.getSomejson();

        assertThat(catch1).isSameAs(catch2);
    }

    @Test
    void whenTheBoundClassLikesCaching() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));

        BoundCachedClass bound = new BoundCachedClass();
        bindAnnotatedFields(loader, bound);

        Catchphrase catch1 = bound.getSomejson();
        bindAnnotatedFields(loader, bound);
        Catchphrase catch2 = bound.getSomejson();

        assertThat(catch1).isSameAs(catch2);
    }

    @Test
    void canProvideAsSupplier() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));

        BoundToSupplier bound = new BoundToSupplier();
        bindAnnotatedFields(loader, bound);

        assertThat(bound.getSomejson().get().getName()).isEqualTo("Gadget");
    }

    @Test
    void canLoadStaticUsingDefaults() throws Exception {
        var loader = new TestDataLoader();
        loader.addPath(Paths.get("loader"));

        bindAnnotatedStaticFields(loader, StaticBoundClass.class);

        assertThat(StaticBoundClass.getSomejson().getName()).isEqualTo("Gadget");
    }

    @Test
    void canInjectLoaderIntoField() throws Exception {
        var loader = new TestDataLoader();

        class ReceiveLoader {
            @Loader
            private TestDataLoader testDataLoader;
        }

        var testObject = new ReceiveLoader();

        bindAnnotatedFields(loader, testObject);

        assertThat(testObject.testDataLoader).isSameAs(loader);
    }

    static class ReceiveStaticLoader {
        @Loader
        private static TestDataLoader testDataLoader;
    }

    @Test
    void canInjectLoaderIntoStaticField() throws Exception {
        var loader = new TestDataLoader();

        bindAnnotatedStaticFields(loader, ReceiveStaticLoader.class);

        assertThat(ReceiveStaticLoader.testDataLoader).isSameAs(loader);
    }

    @Test
    void canReadTestDataLoaderFromTestObjectStatic() throws Exception {
        var loader = new TestDataLoader();

        ReceiveStaticLoader.testDataLoader = loader;

        assertThat(TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(ReceiveStaticLoader.class, null)
                        .get())
                .isSameAs(loader);
    }

    @Test
    void canReadTestDataLoaderFromTestObject() {
        var loader = new TestDataLoader();

        class StoreLoader {
            @Loader
            private TestDataLoader testDataLoader;
        }

        var testObject = new StoreLoader();

        assertThat(TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(null, testObject))
                .isEmpty();

        testObject.testDataLoader = loader;

        assertThat(TestDataLoaderAnnotations.getLoaderFromTestClassOrObject(null, testObject)
                        .get())
                .isSameAs(loader);
    }
}
