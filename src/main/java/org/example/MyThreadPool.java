package org.example;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description:
 * @author: xiaoming
 * @date: 2022/09/28 16:32
 */
public class MyThreadPool<Job extends Runnable> implements ThreadPool<Job> {

    // 设置最大线程数量
    private final static int MAX_THREAD_NUM = 20;
    // 设置默认线程数目
    private final static int DEFAULT_THREAD_NUM = 5;
    // 设置最小维持运行的线程数
    private final static int MIN_THREAD_NUM = 1;
    // 工作的线程数目
    private int workNum;
    // 设置当前工作的队列
    Deque<Job> jobs = new ArrayDeque<>();
    // 工作的线程的列表
    private ArrayBlockingQueue<Worker> workers = new ArrayBlockingQueue<>(MAX_THREAD_NUM);

    // 工作线程编号生成
    private AtomicLong atomicLong = new AtomicLong();

    public MyThreadPool() {
        workNum = DEFAULT_THREAD_NUM;
        init(workNum);
    }

    public MyThreadPool(int workNum) {
        workNum = Math.min(workNum, MAX_THREAD_NUM);
        this.workNum = workNum;
        init(workNum);
    }

    public void init(int num) {
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            // 启动线程
            new Thread(worker).run();
            // 将启动的线程加入到定义的线程列表之中
            workers.add(worker);
        }
    }

    @Override
    public void execute(Job job) {
        if (jobs == null) return;
        synchronized (jobs) {
            jobs.offer(job);
            jobs.notify();
        }

    }

    @Override
    public void shutdown() {
        workers.forEach(Worker::shutdown);
    }

    @Override
    public void addThreadNum(int num) {
        if (num + workNum > MAX_THREAD_NUM) num = MIN_THREAD_NUM - workNum;
        init(num);
        workNum += num;
    }

    @Override
    public void reduceThreadNum(int num) {
        if (num > workNum) throw new RuntimeException(String.format("当前没有%d个线程", num));
    }

    @Override
    public int getCurWaitingNum() {
        return jobs.size();
    }

    class Worker implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                Job job = null;
                synchronized (jobs) {
                    if (jobs.isEmpty()) {
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        job = jobs.getFirst();
                    }
                    if (job == null) return;
                    job.run();
                }
            }
        }

        public void shutdown() {
            running = false;
        }
    }
}
