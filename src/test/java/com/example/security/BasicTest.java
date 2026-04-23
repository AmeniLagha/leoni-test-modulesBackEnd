package com.example.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicTest {

    @Test
    void testAlwaysPasses() {
        assertTrue(true, "Ce test devrait toujours passer");
    }
}