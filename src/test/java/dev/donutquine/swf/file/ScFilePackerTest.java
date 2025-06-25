package dev.donutquine.swf.file;

import dev.donutquine.swf.file.exceptions.UnknownFileVersionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ScFilePackerTest {
    @Test
    void pack() {
        assertThrows(UnknownFileVersionException.class, () -> ScFilePacker.pack(new byte[0], new byte[0], 0));
        assertThrows(UnknownFileVersionException.class, () -> ScFilePacker.pack(new byte[0], new byte[0], 6));
    }
}