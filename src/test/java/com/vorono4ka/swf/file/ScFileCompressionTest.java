package com.vorono4ka.swf.file;

import com.vorono4ka.swf.file.exceptions.FileVerificationException;
import com.vorono4ka.swf.file.exceptions.UnknownFileVersionException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ScFileCompressionTest {
    @Test
    void decompress() throws UnknownFileVersionException, IOException, FileVerificationException {
        byte[] data = {0, 1, 2, 3, 4};
        assertCompressionCycle(data, 1);
        assertCompressionCycle(data, 2);
        assertCompressionCycle(data, 3);
        assertCompressionCycle(data, 4);
        assertCompressionCycle(data, 5);
    }

    private static void assertCompressionCycle(byte[] data, int version) throws IOException, UnknownFileVersionException, FileVerificationException {
        byte[] compressedData = ScFilePacker.pack(data, new byte[0], version);
        ScFileInfo scFileInfo = ScFileUnpacker.unpack(compressedData);
        byte[] decompressedData = scFileInfo.data();

        assertEquals(version == 4 ? 1 : version, scFileInfo.version());
        assertArrayEquals(data, decompressedData);
    }
}