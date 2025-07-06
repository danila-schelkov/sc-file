package dev.donutquine.swf.file.compression;

import org.sevenzip.compression.LZMA.Decoder;
import org.sevenzip.compression.LZMA.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class Lzma {
    private static final int UNCOMPRESSED_SIZE_LENGTH = 4;

    private Lzma() {
    }

    public static byte[] decompress(DataInputStream stream) throws IOException {
        Decoder decoder = new Decoder();

        byte[] decoderProperties = stream.readNBytes(5);
        decoder.setDecoderProperties(decoderProperties);

        int outSize = 0;
        for (int i = 0; i < UNCOMPRESSED_SIZE_LENGTH; i++) {
            outSize |= (stream.read() & 0xFF) << (i * 8);
        }

        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        decoder.code(stream, outputArray, outSize);

        return outputArray.toByteArray();
    }

    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();

        Encoder encoder = new Encoder();
        encoder.writeCoderProperties(outputArray);
        for (int i = 0; i < UNCOMPRESSED_SIZE_LENGTH; i++) {
            outputArray.write((data.length >> (8 * i)) & 0xFF);
        }
        encoder.code(byteArrayInputStream, outputArray, null);

        return outputArray.toByteArray();
    }
}
