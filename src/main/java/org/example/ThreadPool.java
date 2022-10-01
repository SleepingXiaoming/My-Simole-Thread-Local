package org.example;

/**
 * @description:
 * @author: xiaoming
 * @date: 2022/09/28 16:21
 */
public interface ThreadPool<Job extends Runnable> {
    // 使用线程池执行一个任务
    void execute(Job job);

    // 关闭线程池
    void shutdown();

    // 增加线程池中线程数量
    void addThreadNum(int num);

    // 减少线程池中线程的数量
    void reduceThreadNum(int num);

    // 获取当前排队等待的任务数量
    int getCurWaitingNum();
}
