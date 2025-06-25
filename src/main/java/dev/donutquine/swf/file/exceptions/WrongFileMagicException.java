package dev.donutquine.swf.file.exceptions;

public class WrongFileMagicException extends FileVerificationException {
    public WrongFileMagicException(String message) {
        super(message);
    }
}
