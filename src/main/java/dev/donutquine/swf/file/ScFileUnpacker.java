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
import java.util.Arrays;

public final class ScFileUnpacker {
    private static final int SC_MAGIC = 0x5343;

    private ScFileUnpacker() {
    }

    public static ScFileInfo unpack(byte[] compressedData) throws UnknownFileVersionException, IOException, FileVerificationException {
        DataInputStream stream = createDataInputStreamFromBytes(compressedData);

        verifyMagic(stream);

        byte[] hash = null;

        int version = parseVersion(stream);
        switch (version) {
            case 1, 2, 3 -> {
                int hashLength = stream.readInt();
                hash = readBytes(hashLength, stream);
            }
            // Note: returns compressed data to allow user parse metadata and other compressed chunks
            case 5, 6 -> {
                int flagsMaybe;
                if (version == 6) {
                    flagsMaybe = Short.reverseBytes(stream.readShort()) & 0xFFFF;
                } else {
                    flagsMaybe = 0;  // TODO: ?
                }

                byte[] data = Arrays.copyOfRange(compressedData, compressedData.length - stream.available(), compressedData.length);

                return new ScFileInfo(version, flagsMaybe, data);
            }
        }

        byte[] decompressed = decompress(compressedData, version, stream);

        if (hash != null && !Hasher.verifyHash(decompressed, hash)) {
            throw new HashVerificationException("Decompressed data hash doesn't equal to hash from file");
        }

        return new ScFileInfo(version, 0, decompressed);
    }

    private static byte[] decompress(byte[] compressedData, int version, DataInputStream stream) throws IOException, UnknownFileVersionException {
        byte[] decompressed;

        switch (version) {
            case 1 -> decompressed = Lzma.decompress(stream);
            case 2, 3 -> {
                int offset = compressedData.length - stream.available();
                decompressed = Zstandard.decompress(compressedData, offset);
            }
            default ->
                throw new UnknownFileVersionException("Unsupported file version: " + version);
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

    private static void verifyMagic(DataInputStream stream) throws IOException, WrongFileMagicException {
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

        if (version <= 4) {
            return version;
        }

        return Integer.reverseBytes(version);
    }

    private static DataInputStream createDataInputStreamFromBytes(byte[] compressedData) {
        return new DataInputStream(new ByteArrayInputStream(compressedData));
    }
}
