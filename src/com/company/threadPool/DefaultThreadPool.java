package com.company.threadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {

    /**
     * 线程池最大限制数
     */
    private static final int MAX_WORKER_NUMBERS=10;

    /**
     * 线程池默认数量
     */
    private static final int DEFAULT_WORKER_NUMBERS=5;

    /**
     * 线程池最小数量
     */
    private static final int MIN_WORKER_NUMBERS=1;

    /**
     * 工作者线程数量
     */
    private int workerNum=DEFAULT_WORKER_NUMBERS;

    /**
     * job列表，向其中插入job
     */
    private final LinkedList<Job> jobs=new LinkedList<>();

    /**
     * 工作者列表
     */
    private final List<Worker> workers= Collections.synchronizedList(new ArrayList<>());

    /**
     * 线程编号生成
     */
    private AtomicLong threadNum=new AtomicLong();

    public DefaultThreadPool() {
        initializeWorkers(DEFAULT_WORKER_NUMBERS);
    }

    public DefaultThreadPool(int workerNum) {
        this.workerNum = workerNum>MAX_WORKER_NUMBERS?MAX_WORKER_NUMBERS:
                workerNum<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:workerNum;
        initializeWorkers(workerNum);
    }

    @Override
    public void execute(Job job) {
        if(job==null){
            return;
        }
        synchronized (jobs){
            jobs.addLast(job);
            //使用notify是因为能够确定有工作者线程被唤醒，notify代价更小，避免将所有工作者线程移到阻塞队列中
            jobs.notify();
        }
    }

    @Override
    public void shutDown() {
        workers.forEach(t->{
            t.shutDown();
        });
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            if(num+this.workerNum>MAX_WORKER_NUMBERS){
                num=MAX_WORKER_NUMBERS-this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum+=num;
        }
    }

    @Override
    public void removeWorkers(int num) {
        synchronized (jobs){
            if(num>=this.workerNum){
                throw new IllegalArgumentException("beyond num");
            }
            int count=0;
            while(count<num){
                Worker worker= workers.get(count);
                if(workers.remove(worker)){
                    worker.shutDown();
                    count++;
                }
            }
            this.workerNum-=num;
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }

    /**
     * 初始化线程工作者
     * @param num
     */
    private void initializeWorkers(int num){
        for(int i=0;i<num;i++){
            Worker worker=new Worker();
            workers.add(worker);
            Thread t=new Thread(worker,"ThreadPool-Worker-"+threadNum.incrementAndGet());
            t.start();
        }
    }

    class Worker implements Runnable{

        private volatile boolean running=true;

        @Override
        public void run() {
            while (running){
                Job job=null;
                synchronized (jobs){
                    while (jobs.isEmpty()){
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job=jobs.removeFirst();
                }
                if(job!=null){
                    try {
                        job.run();
                    }catch (Exception e){

                    }
                }
            }
        }

        public void shutDown(){
            running=false;
        }
    }
}
