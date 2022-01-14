package io.github.javiercanillas.redisson.utils;

import org.redisson.api.RLock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class helps when dealing with Lock, in this case the Redis locks. For example:
 * <code>
 * RLockable.of(redissonClient.getLock("myLockId"), () -> {
 *     Add code to execute during lock contention
 * }).orElse(null);
 *
 * Check methods to customization
 * </code>
 * @param <T>
 */
public final class RLockable<T> {
    private final RLock lockObject;
    private final Supplier<T> executionBlock;

    private long waitTime = -1;
    private long leaseTime = -1;
    private TimeUnit unit = null;
    private Function<InterruptedException, ? extends RuntimeException> interruptedExceptionSupplier;

    private RLockable(final RLock lockObject, final Supplier<T> executionBlock) {
        this.lockObject = Objects.requireNonNull(lockObject, "lockObject must not be null.");
        this.executionBlock = Objects.requireNonNull(executionBlock, "executionBlock must not be null.");
    }

    /**
     * Throw this method you start creating an instance of this.
     *
     * @param lockObject     a {@link RLock} object. Must be not null.
     * @param executionBlock a {@link Supplier<T>} with the code to execute while locked. Must be not null.
     * @param <T>            The type to be returned by executionBlock
     * @return a {@link RLockable} object for further configuration or execution
     */
    public static <T> RLockable<T> of(final RLock lockObject, final Supplier<T> executionBlock) {
        return new RLockable<>(lockObject, executionBlock);
    }

    /**
     * Configure the time to wait to get the lock. It will be passed to the tryLock method of the {@link RLock} instance if set.
     *
     * @param waitTime wait time value. If less than zero, it means this option shouldn't be used.
     * @return a {@link RLockable} object for further configuration or execution
     */
    public RLockable<T> withWaitTime(final long waitTime) {
        this.waitTime = waitTime;
        return this;
    }

    /**
     * Configure the time to lease the lock (if acquired). It will be passed to the tryLock method of the {@link RLock} instance if set.
     *
     * @param leaseTime lease time value. If less than zero, it means this option shouldn't be be used.
     * @return a {@link RLockable} object for further configuration or execution
     */
    public RLockable<T> withLeaseTime(final long leaseTime) {
        this.leaseTime = leaseTime;
        return this;
    }

    /**
     * Configure the time unit on which {@link RLockable#withLeaseTime(long)} and {@link RLockable#withWaitTime(long)} is expressed.
     * It will be passed to the tryLock method of the {@link RLock} instance if set.
     *
     * @param unit a {@link TimeUnit} defining the unit of measurement of the values leaseTime and waitTime.
     * @return a {@link RLockable} object for further configuration or execution
     */
    public RLockable<T> withTimeUnit(final TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Configure an exception supplier in case a {@link InterruptedException} is thrown while waiting to acquired lock.
     * If it's not present a default {@link RuntimeInterruptedException} will be thrown instead.
     *
     * @param exceptionSupplier method that will supply an Exception to be thrown. It cannot be null.
     * @param <X> must extend from {@link RuntimeException}
     * @return a {@link RLockable} object for further configuration or execution
     */
    public <X extends RuntimeException> RLockable<T> withInterruptedExceptionSupplier(
            final Function<InterruptedException, ? extends X> exceptionSupplier) {
        this.interruptedExceptionSupplier = Objects.requireNonNull(exceptionSupplier, "exceptionSupplier must not be null.");
        return this;
    }

    /**
     * If lock was acquired, execute and returns the executionBlock result, otherwise returns {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *              May be {@code null}.
     * @return execute and returns the executionBlock result, if present, otherwise {@code other}
     * @throws RuntimeInterruptedException if interrupted while waiting to get lock
     */
    public T orElse(final T other) throws RuntimeInterruptedException {
        return this.orElseGet(() -> other);
    }

    /**
     * If lock was acquired, execute and returns the executionBlock result, otherwise otherwise returns the result
     * produced by the supplying function.
     *
     * @param otherSupplier the supplying function that produces a value to be returned
     * @return execute and returns the executionBlock result, if present, otherwise the result produced by the
     * supplying function
     * @throws RuntimeInterruptedException if interrupted while waiting to get lock
     * @throws NullPointerException        if no value is present and the supplying
     *                                     function is {@code null}
     */
    public T orElseGet(final Supplier<T> otherSupplier) throws RuntimeInterruptedException, NullPointerException {
        Objects.requireNonNull(otherSupplier, "otherSupplier must not be null.");
        if (acquireLock()) {
            try {
                return executionBlock.get();
            } finally {
                this.lockObject.unlock();
            }
        } else {
            return otherSupplier.get();
        }
    }

    /**
     * If lock was acquired, execute and returns the executionBlock result, otherwise otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @param otherSupplier the supplying function that produces an
     *                      exception to be thrown
     * @param <X> type of exception
     * @return execute and returns the executionBlock result, if present, otherwise the result produced by the
     * supplying function
     * @throws RuntimeInterruptedException if interrupted while waiting to get lock
     * @throws X                           If lock was not acquired
     * @apiNote A method reference to the exception constructor with an empty argument
     * list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     */
    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> otherSupplier) throws RuntimeInterruptedException, X {
        Objects.requireNonNull(otherSupplier, "otherSupplier must not be null.");
        if (acquireLock()) {
            try {
                return executionBlock.get();
            } finally {
                this.lockObject.unlock();
            }
        } else {
            throw otherSupplier.get();
        }
    }

    boolean acquireLock() throws RuntimeInterruptedException {
        boolean acquired;
        try {
            if (waitTime >= 0 && leaseTime >= 0 && unit != null) {
                acquired = this.lockObject.tryLock(waitTime, leaseTime, unit);
            } else if (waitTime >= 0 && unit != null) {
                acquired = this.lockObject.tryLock(waitTime, unit);
            } else {
                acquired = this.lockObject.tryLock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (this.interruptedExceptionSupplier != null) {
                throw interruptedExceptionSupplier.apply(e);
            } else {
                throw new RuntimeInterruptedException(e);
            }
        }
        return acquired;
    }
}