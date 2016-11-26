package com.socialthingy.plusf.util;

import java.util.function.Consumer;

public class Try<T> {
    private Exception exception;
    private T success;

    public static <S> Try<S> success(final S success) {
        final Try<S> s = new Try<>();
        s.success = success;
        return s;
    }

    public static <S> Try<S> failure(final Exception exception) {
        final Try<S> s = new Try<>();
        s.exception = exception;
        return s;
    }

    private Try() {}

    public void ifSuccess(final Consumer<T> fn) {
        if (success != null) {
            fn.accept(success);
        }
    }

    public void ifFailure(final Consumer<Exception> fn) {
        if (exception != null) {
            fn.accept(exception);
        }
    }
}
