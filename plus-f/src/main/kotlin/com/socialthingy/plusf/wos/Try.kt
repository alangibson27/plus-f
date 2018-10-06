package com.socialthingy.plusf.wos

fun <T> Try(expr: () -> T): Try<T> {
    return try {
        Success(expr())
    } catch (ex: Exception) {
        Failure(ex)
    }
}

sealed class Try<out T> {
    abstract fun <U> map(fn: (T) -> U): Try<U>
    abstract fun ifDefined(fn: (T) -> Unit)
    abstract fun get(): T
}

class Success<out T>(val t: T): Try<T>() {
    override fun <U> map(fn: (T) -> U): Try<U> {
        return Success(fn(t))
    }

    override fun ifDefined(fn: (T) -> Unit) {
        fn(t)
    }

    override fun get(): T {
        return t
    }
}

class Failure<out T>(val ex: Exception): Try<T>() {
    override fun <U> map(fn: (T) -> U): Try<U> {
        return Failure(ex)
    }

    override fun ifDefined(fn: (T) -> Unit) {}

    override fun get(): T {
        throw ex
    }
}