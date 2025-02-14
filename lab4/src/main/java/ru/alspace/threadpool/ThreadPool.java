package ru.alspace.threadpool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private static final Logger logger = LogManager.getLogger(ThreadPool.class);

    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    private final List<Worker> workers = new LinkedList<>();
    private volatile boolean running = true;
    private final int maxQueueSize;

    /**
     * Конструктор ThreadPool с ограниченной очередью.
     *
     * @param numberOfThreads количество рабочих потоков
     * @param maxQueueSize    максимальный размер очереди задач
     */
    public ThreadPool(int numberOfThreads, int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        for (int i = 0; i < numberOfThreads; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * Метод submit теперь блокируется, если очередь заполнена.
     *
     * @param task задача для выполнения
     */
    public void submit(Runnable task) {
        synchronized (taskQueue) {
            while (taskQueue.size() >= maxQueueSize && running) {
                try {
                    taskQueue.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            if (!running) return;
            taskQueue.add(task);
            taskQueue.notifyAll(); // уведомляем рабочих потоков
        }
    }

    public void shutdown() {
        running = false;
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                // Игнорируем прерывания
            }
        }
    }

    public int getQueueSize() {
        synchronized (taskQueue) {
            return taskQueue.size();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (running || !taskQueue.isEmpty()) {
                Runnable task;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty() && running) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            // Игнорируем прерывания
                        }
                    }
                    if (taskQueue.isEmpty()) {
                        break;
                    }
                    task = taskQueue.removeFirst();
                    taskQueue.notifyAll(); // уведомляем производителей, что место освободилось
                }
                try {
                    task.run();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }
}
