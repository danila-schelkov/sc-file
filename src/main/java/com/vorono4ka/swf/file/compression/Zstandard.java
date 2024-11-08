package com.vorono4ka.swf.file.compression;

import com.github.luben.zstd.Zstd;

public final class Zstandard {
    private Zstandard() {
    }

    public static byte[] decompress(byte[] compressedData, int offset) {
        return decompress(compressedData, offset, compressedData.length - offset);
    }

    public static byte[] decompress(byte[] compressedData, int offset, int length) {
        int decompressedSize = (int) Zstd.getFrameContentSize(compressedData, offset, length);

        byte[] zstdContent;
        if (offset > 0 || length != compressedData.length - offset) {
            zstdContent = new byte[length];
            System.arraycopy(compressedData, offset, zstdContent, 0, zstdContent.length);
        } else {
            zstdContent = compressedData;
        }

        return Zstd.decompress(
            zstdContent,
            decompressedSize
        );
    }

    public static byte[] compress(byte[] data) {
        return Zstd.compress(data);
    }
}
