package io.github.javiercanillas.redisson.utils;

public final class RuntimeInterruptedException extends RuntimeException {
    static final long serialVersionUID = -3612888940997972536L;

    public RuntimeInterruptedException(final InterruptedException inner) {
        super(inner);
    }
}
