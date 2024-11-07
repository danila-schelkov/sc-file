package com.vorono4ka.swf.file;

import com.vorono4ka.swf.file.compression.Lzma;
import com.vorono4ka.swf.file.compression.Zstandard;
import com.vorono4ka.swf.file.exceptions.UnknownFileVersionException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ScFilePacker {
    private static final byte[] LZMA_PROPERTIES = new byte[]{0x5d, 0x00, 0x00, 0x04, 0x00};

    private static final int SC_MAGIC = 0x5343;

    private ScFilePacker() {
    }

    public static byte[] pack(byte[] data, byte[] metadata, int version) throws IOException, UnknownFileVersionException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(outputStream);

        dos.writeShort(SC_MAGIC);

        dos.writeInt(version);
        if (version == 4) {
            version = 1;
            dos.writeInt(version);
        }

        if (version < 5) {
            byte[] hash = Hasher.createHash(data);

            dos.writeInt(hash.length);
            dos.write(hash);
        } else {
            dos.writeInt(metadata.length);
            dos.write(metadata);
        }

        switch (version) {
            case 1 -> {
                dos.write(LZMA_PROPERTIES);
                for (int i = 0; i < 4; i++) {
                    dos.writeByte((data.length >> (8 * i)) & 0xFF);
                }

                dos.write(Lzma.compressLzma(data));
            }
            case 2, 3, 5 -> dos.write(Zstandard.compress(data));
            default ->
                throw new UnknownFileVersionException("Unknown file version: " + version);
        }

        return outputStream.toByteArray();
    }
}
