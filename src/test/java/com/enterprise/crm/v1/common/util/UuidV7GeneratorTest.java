package com.enterprise.crm.v1.common.util;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UuidV7GeneratorTest {

    @Test
    public void testGenerateValidUuidV7() {
        UUID uuid = UuidV7Generator.generate();
        assertNotNull(uuid);
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant()); // RFC 4122 variant
    }

    @Test
    public void testSequentialOrder() throws InterruptedException {
        UUID first = UuidV7Generator.generate();
        Thread.sleep(5);
        UUID second = UuidV7Generator.generate();
        
        // Assert that the string comparison or long comparison reflects chronological order
        assertTrue(first.toString().compareTo(second.toString()) < 0);
    }
}
