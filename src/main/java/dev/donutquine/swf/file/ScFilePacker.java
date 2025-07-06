package dev.donutquine.swf.file;

import dev.donutquine.swf.file.compression.Lzma;
import dev.donutquine.swf.file.compression.Zstandard;
import dev.donutquine.swf.file.exceptions.UnknownFileVersionException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ScFilePacker {
    private static final int SC_MAGIC = 0x5343;

    private ScFilePacker() {
    }

    public static byte[] pack(byte[] data, byte[] metadata, int version) throws IOException, UnknownFileVersionException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);

        dos.writeShort(SC_MAGIC);

        if (version < 5) {
            dos.writeInt(version);
            if (version == 4) {
                version = 1;
                dos.writeInt(version);
            }
        } else {
            dos.writeInt(Integer.reverseBytes(version));
        }

        if (version < 5) {
            byte[] hash = Hasher.calculateHash(data);

            dos.writeInt(hash.length);
            dos.write(hash);
        } else {
            if (version == 6) {
                dos.writeShort(0);
            }

            dos.writeInt(metadata.length);
            dos.write(metadata);
        }

        switch (version) {
            case 1 -> {
                dos.write(Lzma.compress(data));
            }
            case 2, 3, 5, 6 -> dos.write(Zstandard.compress(data));
            default ->
                throw new UnknownFileVersionException("Unknown file version: " + version);
        }

        return outputStream.toByteArray();
    }
}
