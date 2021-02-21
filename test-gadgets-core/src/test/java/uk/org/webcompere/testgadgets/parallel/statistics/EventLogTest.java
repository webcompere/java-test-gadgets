package uk.org.webcompere.testgadgets.parallel.statistics;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class EventLogTest {

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EventLog.Timepoint.class).verify();
    }
}
