package dev.donutquine.swf.file.compression;

import com.github.luben.zstd.Zstd;

public final class Zstandard {
    private Zstandard() {
    }

    public static byte[] decompress(byte[] compressedData, int offset) {
        return Zstd.decompressFrame(
            compressedData,
            offset
        );
    }

    public static byte[] compress(byte[] data) {
        return Zstd.compress(data);
    }
}
