package io.github.mityavasilyev.zeebedevhelper.exception;

public class TerminalException extends RuntimeException {
    public TerminalException() {
        super();
    }

    public TerminalException(String message) {
        super(message);
    }

    public TerminalException(String message, Throwable cause) {
        super(message, cause);
    }
}
