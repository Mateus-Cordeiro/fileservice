package com.github.mateuscordeiro.fileservice.concurrency;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class FileConcurrencyManager {
    private final Map<Path, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public void withLock(Path path, Runnable action) {
        ReentrantLock lock = lockMap.computeIfAbsent(path.toAbsolutePath().normalize(), p -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}