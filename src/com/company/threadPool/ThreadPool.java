package com.company.threadPool;

public interface ThreadPool<Job extends Runnable> {
    /**
     * 执行job
     * @param job
     */
    void execute(Job job);

    /**
     * 关闭线程池
     */
    void shutDown();

    /**
     * 增加工作者线程
     */
    void addWorkers(int num);

    /**
     * 减少工作者线程
     * @param num
     */
    void removeWorkers(int num);

    /**
     * 得到正在等待执行的任务数量
     * @return
     */
    int getJobSize();
}
