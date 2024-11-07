package com.vorono4ka.swf.file.exceptions;

public class WrongFileMagicException extends FileVerificationException {
    public WrongFileMagicException(String message) {
        super(message);
    }
}
