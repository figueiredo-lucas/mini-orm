package br.mini.exception;

/**
 * Exception base do mini-orm
 *
 * @author figueiredo-lucas
 */
public class MiniException extends Exception {

    private static final long serialVersionUID = 4821951982495189382L;

    public MiniException() {
        super();
    }

    public MiniException(final String message, final Throwable exception) {
        super(message, exception);
    }

    public MiniException(final String message) {
        super(message);
    }

    public MiniException(final Throwable exception) {
        super(exception);
    }

}
