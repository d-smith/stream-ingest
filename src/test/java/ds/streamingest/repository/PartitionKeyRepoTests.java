package ds.streamingest.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import ds.streamingest.model.*;

//@SpringBootTest
public class PartitionKeyRepoTests {

    @Test
    public void storeAndGet() {
        PartitionKeyExtractorDescRepo repository = new PartitionKeyExtractorDescRepo();
        PartitionKeyExtractorDescription d = new PartitionKeyExtractorDescription("foo", PartitionKeyExtractorTypes.HEADER, "PKEY_HEADER");
        repository.store(d);
        PartitionKeyExtractorDescription retrieved = repository.retrieve("foo");
        assertNotNull(retrieved);
        assertEquals(retrieved.getExtractionContext(), "PKEY_HEADER");
    }
}