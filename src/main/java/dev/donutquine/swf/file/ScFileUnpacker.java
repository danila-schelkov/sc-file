package dev.donutquine.swf.file;

import dev.donutquine.swf.file.compression.Lzma;
import dev.donutquine.swf.file.compression.Zstandard;
import dev.donutquine.swf.file.exceptions.FileVerificationException;
import dev.donutquine.swf.file.exceptions.HashVerificationException;
import dev.donutquine.swf.file.exceptions.UnknownFileVersionException;
import dev.donutquine.swf.file.exceptions.WrongFileMagicException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class ScFileUnpacker {
    private static final int SC_MAGIC = 0x5343;
    private static final int FIVE_LITTLE_ENDIAN = swapEndian32(5);
    private static final byte[] START_SECTION_BYTES = {'S', 'T', 'A', 'R', 'T'};

    private ScFileUnpacker() {
    }

    public static ScFileInfo unpack(byte[] compressedData) throws UnknownFileVersionException, IOException, FileVerificationException {
        DataInputStream stream = createDataInputStreamFromBytes(compressedData);

        checkMagic(stream);

        byte[] hash = null;

        int version = parseVersion(stream);
        switch (version) {
            case 1, 2, 3 -> {
                int hashLength = stream.readInt();
                hash = readBytes(hashLength, stream);
            }
            case 5 -> {
                int metadataRootTableOffset = swapEndian32(stream.readInt());
                skipBytes(stream, metadataRootTableOffset);
            }
        }

        byte[] decompressed = decompress(compressedData, version, stream);

        if (hash != null && !Hasher.verifyHash(decompressed, hash)) {
            throw new HashVerificationException("Decompressed data hash doesn't equal to hash from file");
        }

        return new ScFileInfo(version, decompressed);
    }

    private static byte[] decompress(byte[] compressedData, int version, DataInputStream stream) throws IOException, UnknownFileVersionException {
        byte[] decompressed;

        switch (version) {
            case 1 -> decompressed = Lzma.decompress(stream);
            case 2, 3, 5 -> {
                int offset = compressedData.length - stream.available();
                int startSectionOffset = indexOf(compressedData, START_SECTION_BYTES);
                if (startSectionOffset != -1) {
                    decompressed = Zstandard.decompress(compressedData, offset, startSectionOffset - offset);
                } else {
                    decompressed = Zstandard.decompress(compressedData, offset);
                }
            }
            default ->
                throw new UnknownFileVersionException("Unknown file version: " + version);
        }

        return decompressed;
    }

    private static byte[] readBytes(int hashLength, DataInputStream stream) throws IOException {
        byte[] data = new byte[hashLength];
        int read = stream.read(data);
        if (read != hashLength) {
            throw new IllegalStateException("The number of bytes read is not equal to the requested number of bytes: " + read + " vs " + hashLength);
        }

        return data;
    }

    private static void checkMagic(DataInputStream stream) throws IOException, WrongFileMagicException {
        int magic = stream.readShort();
        if (magic != SC_MAGIC) {
            throw new WrongFileMagicException("Unknown file magic: " + magic);
        }
    }

    private static int parseVersion(DataInputStream stream) throws IOException {
        int version = stream.readInt();
        if (version == 4) {
            version = stream.readInt();
        }
        if (version == FIVE_LITTLE_ENDIAN) {
            version = 5;
        }
        return version;
    }

    private static DataInputStream createDataInputStreamFromBytes(byte[] compressedData) {
        return new DataInputStream(new ByteArrayInputStream(compressedData));
    }

    private static void skipBytes(DataInputStream stream, int bytesToBeSkipped) throws IOException {
        long actualSkipped = stream.skip(bytesToBeSkipped);
        if (actualSkipped != bytesToBeSkipped) {
            throw new IllegalStateException("The number of bytes skipped is not equal to the requested number of bytes: " + actualSkipped + " vs " + bytesToBeSkipped);
        }
    }

    private static int swapEndian32(int metadataRootTableOffset) {
        return (metadataRootTableOffset >> 24) & 0xFF | (((metadataRootTableOffset >> 16) & 0xFF) << 8) | (((metadataRootTableOffset >> 8) & 0xFF) << 16) | ((metadataRootTableOffset & 0xFF) << 24);
    }

    private static int indexOf(byte[] array, byte[] bytesToFind) {
        for (int i = 0; i < array.length; i++) {
            boolean found = true;
            for (int j = 0; j < bytesToFind.length; j++) {
                if (array[i+j] != bytesToFind[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return i;
            }
        }

        return -1;
    }
}
