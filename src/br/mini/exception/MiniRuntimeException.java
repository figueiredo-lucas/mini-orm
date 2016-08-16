package br.mini.exception;

/**
 * Exception base do mini-orm
 *
 * @author figueiredo-lucas
 */
public class MiniRuntimeException extends RuntimeException {

    public MiniRuntimeException() {
        super();
    }

    public MiniRuntimeException(final String message, final Throwable exception) {
        super(message, exception);
    }

    public MiniRuntimeException(final String message) {
        super(message);
    }

    public MiniRuntimeException(final Throwable exception) {
        super(exception);
    }

}
