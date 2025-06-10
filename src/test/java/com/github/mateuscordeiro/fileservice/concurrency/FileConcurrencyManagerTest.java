package com.github.mateuscordeiro.fileservice.concurrency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

class FileConcurrencyManagerTest {
    private final FileConcurrencyManager manager = new FileConcurrencyManager();

    @Test
    void withLock_allowsExclusiveAccessPerPath() throws InterruptedException {
        Path file = Path.of("a.txt");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AtomicInteger counter = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(2);

        Runnable task = () -> manager.withLock(file, () -> {
            int value = counter.get();
            try {
                Thread.sleep(100); // simulate contention
            } catch (InterruptedException ignored) {
            }
            counter.set(value + 1);
            latch.countDown();
        });

        executor.submit(task);
        executor.submit(task);

        latch.await(2, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(2, counter.get());
    }

    @Test
    void withLock_doesNotBlockOtherPaths() throws InterruptedException {
        Path file1 = Path.of("file1.txt");
        Path file2 = Path.of("file2.txt");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean file1Locked = new AtomicBoolean(false);
        AtomicBoolean file2Locked = new AtomicBoolean(false);

        executor.submit(() -> {
            manager.withLock(file1, () -> {
                file1Locked.set(true);
                latch.countDown();
            });
        });

        executor.submit(() -> {
            manager.withLock(file2, () -> {
                file2Locked.set(true);
                latch.countDown();
            });
        });

        latch.await(2, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(file1Locked.get());
        assertTrue(file2Locked.get());
    }

    @Test
    void withLock_releasesLockAfterException() {
        Path file = Path.of("fail.txt");

        assertThrows(RuntimeException.class, () -> manager.withLock(file, () -> {
            throw new RuntimeException("boom");
        }));

        // Lock should be available again
        assertDoesNotThrow(() -> manager.withLock(file, () -> {
            // success on retry
        }));
    }

    @Test
    void withLock_releasesLockAfterExecution() throws Exception {
        Path path = Path.of("somefile.txt");

        manager.withLock(path, () -> {
            // no-op
        });

        Field lockMapField = FileConcurrencyManager.class.getDeclaredField("lockMap");
        lockMapField.setAccessible(true);
        Map<Path, ReentrantLock> lockMap = (Map<Path, ReentrantLock>) lockMapField.get(manager);

        assertFalse(lockMap.containsKey(path));
    }
}