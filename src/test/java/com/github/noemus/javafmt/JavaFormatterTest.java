package com.github.noemus.javafmt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaFormatterTest {
    @Test
    void missing_source_root() {
        var invalidUsage = assertThrows(
            InvalidUsage.class,
            JavaFormatter::main
        );
        var message = invalidUsage.getMessage();
        assertTrue(message.startsWith("\n====="));

        var usageText = message.replace("=", "").trim();
        assertTrue(usageText.startsWith("Usage:"));
    }
}