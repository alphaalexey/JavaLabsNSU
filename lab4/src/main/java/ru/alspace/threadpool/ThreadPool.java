package ru.alspace.threadpool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private static final Logger logger = LogManager.getLogger(ThreadPool.class);

    private final LinkedList<Runnable> taskQueue = new LinkedList<>();
    private final List<Worker> workers = new LinkedList<>();
    private final int maxQueueSize;
    private volatile boolean running = true;

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
        // Очистим очередь, если оставшиеся задачи не критичны
        synchronized (taskQueue) {
            taskQueue.clear();
            taskQueue.notifyAll();
        }
        // Прерываем ожидание у рабочих потоков
        for (Worker worker : workers) {
            worker.interrupt();
        }
        // Ждем завершения рабочих потоков с таймаутом, чтобы не зависнуть
        for (Worker worker : workers) {
            try {
                worker.join(1000); // ждем максимум 1 секунду на каждого
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
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
                            // Если прерывание произошло, проверяем условие цикла
                            if (!running) {
                                return;
                            }
                        }
                    }
                    if (taskQueue.isEmpty()) {
                        continue;
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
