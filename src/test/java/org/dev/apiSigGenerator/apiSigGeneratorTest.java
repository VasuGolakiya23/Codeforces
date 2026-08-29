package org.dev.apiSigGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class apiSigGeneratorTest {

    private static final long FIXED_TIME = 1700000000L;

    private apiSigGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new apiSigGenerator();
        generator.apiKey = "testkey";
        generator.apiSecret = "testsecret";
        generator.randomString = "123456";
    }

    private Map<String, String> handlesParam() {
        Map<String, String> params = new HashMap<>();
        params.put("handles", "tourist");
        return params;
    }

    @Test
    void producesTheDigestCodeforcesExpects() {
        String expected = "123456"
                + "d8e0b490eaacbfe4ba3cc802e9039cfbe4ec89a0373059cc4b8ca0f159427750"
                + "d17e2ce31cea6d789a9c4f82976b54d6dab986a27a82f180247a7de73d1237ec";

        assertEquals(expected, generator.createApiSig("user.info", handlesParam(), FIXED_TIME));
    }

    @Test
    void isPrefixedWithTheRandomStringAndA128CharDigest() {
        String sig = generator.createApiSig("user.info", handlesParam(), FIXED_TIME);

        assertTrue(sig.startsWith("123456"), "signature must start with the random string");
        assertEquals(6 + 128, sig.length());
    }

    @Test
    void sortsParametersRegardlessOfInsertionOrder() {
        Map<String, String> ascending = new java.util.LinkedHashMap<>();
        ascending.put("aaa", "1");
        ascending.put("zzz", "2");

        Map<String, String> descending = new java.util.LinkedHashMap<>();
        descending.put("zzz", "2");
        descending.put("aaa", "1");

        assertEquals(
                generator.createApiSig("user.info", ascending, FIXED_TIME),
                generator.createApiSig("user.info", descending, FIXED_TIME));
    }

    @Test
    void signatureIsBoundToTheSuppliedTime() {
        assertNotEquals(
                generator.createApiSig("user.info", handlesParam(), FIXED_TIME),
                generator.createApiSig("user.info", handlesParam(), FIXED_TIME + 1));
    }
}
