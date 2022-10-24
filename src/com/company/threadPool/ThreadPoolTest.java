package com.company.threadPool;

public class ThreadPoolTest {

    public static void main(String[] args){
        Runnable runner=()->{
            System.out.println("hello:"+ Thread.currentThread().getId());
        };
        ThreadPool<Runnable> threadPool=new DefaultThreadPool<Runnable>(5);
        for(int i=0;i<6;i++){
            threadPool.execute(runner);
        }
    }

}
