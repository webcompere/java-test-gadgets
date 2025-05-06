package uk.org.webcompere.testgadgets.testdatafactory;

public enum Immutable {
    // the default for a test loader is no caching - MUTABLE, but
    // for fields and parameters may be set for the fixture
    DEFAULT,

    // treat objects as immutable and cache them
    IMMUTABLE,

    // the object is likely to change so give me a fresh one
    MUTABLE,
}
