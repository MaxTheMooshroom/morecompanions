package net.qiyanamark.companionpouch.exception;

public class ExceptionUnreachable extends RuntimeException {
    public ExceptionUnreachable() {
        super("This path is unreachable");
    }
}
