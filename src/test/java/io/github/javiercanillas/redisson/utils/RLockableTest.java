package io.github.javiercanillas.redisson.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RLockableTest {

    @Mock
    RLock lock;
    @Mock
    Supplier<Object> executionBlock;
    @Mock
    Object executionResult;
    @Mock
    Object otherObject;

    @BeforeEach
    void setUp() {
    }

    @Test
    void acquiredLockAndSuccessfulExecution01() {
        doReturn(true).when(lock).tryLock();
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock).orElse(otherObject));
        verify(lock, times(1)).tryLock();
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock();
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock).orElseGet(() -> null));
        verify(lock, times(1)).tryLock();
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock();
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock).orElseThrow(() -> new RuntimeException()));
        verify(lock, times(1)).tryLock();
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void acquiredLockAndSuccessfulExecution02() throws InterruptedException {
        doReturn(true).when(lock).tryLock(1, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withWaitTime(1)
                .orElse(otherObject));
        verify(lock, times(1)).tryLock(1, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock(1, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withWaitTime(1)
                .orElseGet(() -> null));
        verify(lock, times(1)).tryLock(1, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock(1, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withWaitTime(1)
                .orElseThrow(() -> new RuntimeException()));
        verify(lock, times(1)).tryLock(1, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void acquiredLockAndSuccessfulExecution03() throws InterruptedException {
        doReturn(true).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1)
                .orElse(otherObject));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1)
                .orElseGet(() -> null));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);

        reset(lock, executionBlock);

        doReturn(true).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        doReturn(executionResult).when(executionBlock).get();
        assertEquals(executionResult, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1)
                .orElseThrow(() -> new RuntimeException()));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verify(executionBlock, times(1)).get();
        verify(lock, times(1)).unlock();
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndReturnedOther01() {
        doReturn(false).when(lock).tryLock();
        assertEquals(otherObject, RLockable.of(lock, executionBlock).orElse(otherObject));
        verify(lock, times(1)).tryLock();
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndReturnedOther02() throws InterruptedException {
        doReturn(false).when(lock).tryLock(1, TimeUnit.MILLISECONDS);
        assertEquals(otherObject, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withWaitTime(1)
                .orElse(otherObject));
        verify(lock, times(1)).tryLock(1, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndReturnedOther03() throws InterruptedException {
        doReturn(false).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        assertEquals(otherObject, RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1)
                .orElse(otherObject));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndThrowError01() {
        doReturn(false).when(lock).tryLock();
        var ex = new RuntimeException("test");
        final var objectRLockable = RLockable.of(lock, executionBlock);
        assertEquals(ex, assertThrows(RuntimeException.class, () ->
                objectRLockable.orElseThrow(() -> ex)));
        verify(lock, times(1)).tryLock();
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndThrowError02() throws InterruptedException {
        doReturn(false).when(lock).tryLock(1, TimeUnit.MILLISECONDS);
        var ex = new RuntimeException("test");
        final var objectRLockable = RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withWaitTime(1);
        assertEquals(ex, assertThrows(RuntimeException.class, () ->
                objectRLockable.orElseThrow(() -> ex)));
        verify(lock, times(1)).tryLock(1, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void notAcquiredLockAndThrowError03() throws InterruptedException {
        doReturn(false).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        var ex = new RuntimeException("test");
        final var objectRLockable = RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1);
        assertEquals(ex, assertThrows(RuntimeException.class, () ->
                objectRLockable.orElseThrow(() -> ex)));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void interruptedWaitingLockDefaultExceptionSupplier() throws InterruptedException {
        doThrow(new InterruptedException()).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        final var objectRLockable = RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1);
        assertThrows(RuntimeException.class, () -> objectRLockable.orElse(null));
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }

    @Test
    void interruptedWaitingLockCustomExceptionSupplier() throws InterruptedException {
        doThrow(new InterruptedException()).when(lock).tryLock(1, 10, TimeUnit.MILLISECONDS);
        final var objectRLockable = RLockable.of(lock, executionBlock)
                .withTimeUnit(TimeUnit.MILLISECONDS)
                .withLeaseTime(10)
                .withWaitTime(1)
                .withInterruptedExceptionSupplier((e) -> new RuntimeException(e));
        var thrownException = assertThrows(RuntimeException.class, () -> objectRLockable.orElse(null));
        assertEquals(RuntimeException.class, thrownException.getClass());
        assertEquals(InterruptedException.class, thrownException.getCause().getClass());
        verify(lock, times(1)).tryLock(1, 10, TimeUnit.MILLISECONDS);
        verifyNoMoreInteractions(lock, executionBlock);
    }
}