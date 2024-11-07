package com.vorono4ka.swf.file.compression;

import com.github.luben.zstd.Zstd;

public final class Zstandard {
    private Zstandard() {
    }

    public static byte[] decompressZstd(byte[] compressedData, int offset) {
        int length = compressedData.length - offset;
        int decompressedSize = (int) Zstd.getFrameContentSize(compressedData, offset, length);

        byte[] zstdContent;
        if (offset > 0) {
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
