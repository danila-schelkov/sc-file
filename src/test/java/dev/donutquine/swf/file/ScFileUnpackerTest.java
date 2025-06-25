package dev.donutquine.swf.file;

import dev.donutquine.swf.file.exceptions.FileVerificationException;
import dev.donutquine.swf.file.exceptions.HashVerificationException;
import dev.donutquine.swf.file.exceptions.UnknownFileVersionException;
import dev.donutquine.swf.file.exceptions.WrongFileMagicException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ScFileUnpackerTest {
    private static final int COMPRESSED_DATA_1234_VERSION = 1;
    private static final byte[] COMPRESSED_DATA_1234 = {83, 67, 0, 0, 0, 1, 0, 0, 0, 16, (byte) 0x81, (byte) 0xdc, (byte) 0x9b, (byte) 0xdb, 82, (byte) 0xd0, 77, (byte) 0xc2, 0, 54, (byte) 0xdb, (byte) 0xd8, 49, 62, (byte) 0xd0, 85, 93, 0, 0, 4, 0, 4, 0, 0, 0, 0, 24, (byte) 0x8c, (byte) 0x82, (byte) 0xb6, (byte) 0xcd, (byte) 0xf4, 91, 42, (byte) 0xff, (byte) 0xfa, 28, 96, 0};
    private static final byte[] DECOMPRESSED_DATA_1234 = {0x31, 0x32, 0x33, 0x34};

    private static final byte[] COMPRESSED_DATA_01234_WRONG_MAGIC = {83, 68};
    private static final byte[] COMPRESSED_DATA_01234_MISSING_BYTES = {83, 67, 0};
    private static final byte[] COMPRESSED_DATA_01234_BROKEN_HASH = {83, 67, 0, 0, 0, 1, 0, 0, 0, 16, (byte) 0x81, (byte) 0xdc, (byte) 0x9b, (byte) 0xdb, 82, (byte) 0xd0, 77, (byte) 0xc2, 0, 54, (byte) 0xdb, (byte) 0xd8, 49, 62, (byte) 0xd0, 85, 93, 0, 0, 1, 0, 5, 0, 0, 0, 0, 24, 12, 66, (byte) 0x92, 106, 101, 77, (byte) 0x8f, 82};

    @Test
    void unpack() throws UnknownFileVersionException, IOException, FileVerificationException {
        ScFileInfo unpacked = ScFileUnpacker.unpack(COMPRESSED_DATA_1234);
        assertEquals(COMPRESSED_DATA_1234_VERSION, unpacked.version());
        assertArrayEquals(DECOMPRESSED_DATA_1234, unpacked.data());
        assertThrows(WrongFileMagicException.class, () -> ScFileUnpacker.unpack(COMPRESSED_DATA_01234_WRONG_MAGIC));
        assertThrows(IOException.class, () -> ScFileUnpacker.unpack(COMPRESSED_DATA_01234_MISSING_BYTES));
        assertThrows(HashVerificationException.class, () -> ScFileUnpacker.unpack(COMPRESSED_DATA_01234_BROKEN_HASH));
    }
}