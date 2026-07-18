package com.enterprise.crm.v1.common.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public class UuidV7Generator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static UUID generate() {
        long valueMs = Instant.now().toEpochMilli();
        long high = valueMs << 16;
        high |= 0x7000L; // Set UUID version 7 bits

        long sequence = RANDOM.nextLong() & 0x0FFFL;
        high |= sequence;

        long low = RANDOM.nextLong();
        low &= 0x3FFFFFFFFFFFFFFFL;
        low |= 0x8000000000000000L; // Set RFC 4122 variant bits

        return new UUID(high, low);
    }
}
