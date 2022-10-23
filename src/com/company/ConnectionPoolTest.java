package com.company;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolTest {
    private static ConnectionPool connectionPool=new ConnectionPool(10);

    //同时开始
    private static CountDownLatch start=new CountDownLatch(1);

    //同时结束
    private static CountDownLatch end;

    public static void main(String[] args) throws InterruptedException {
        int threadCount=20;
        end=new CountDownLatch(threadCount);
        int tryCount=20;
        AtomicInteger got=new AtomicInteger();
        AtomicInteger notGot=new AtomicInteger();
        for(int i=0;i<threadCount;i++){
            Thread thread=new Thread(new ConnectionRunner(tryCount,got,notGot));
            thread.start();
        }
        start.countDown();
        end.await();
        System.out.println("total invoke :"+(threadCount*tryCount));
        System.out.println("get connection :"+got.get());
        System.out.println("not got connection:"+notGot.get());
    }

    static class ConnectionRunner implements Runnable{

        private int tryCount;

        private AtomicInteger got;

        private AtomicInteger notGot;

        public ConnectionRunner(int count, AtomicInteger got, AtomicInteger notGot) {
            this.tryCount = count;
            this.got = got;
            this.notGot = notGot;
        }

        @Override
        public void run() {
            try {
                start.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (tryCount>0){
                try {
                    Connection connection=connectionPool.fetchConnection(1000);
                    if(connection!=null){
                        try {
                            connection.createStatement();
                            connection.commit();
//                            System.out.println("current Thread is :"+Thread.currentThread().getName()+"succeed get connection ,current time is:"+
//                                    new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        }catch (SQLException sQLException){
                            sQLException.printStackTrace();
                        }finally {
//                            System.out.println("current Thread is :"+Thread.currentThread().getName()+" try to release connection ,current time is:"+
//                                    new SimpleDateFormat("HH:mm:ss").format(new Date()));
                            connectionPool.releaseConnection(connection);
                            got.getAndIncrement();
                        }
                    }else{
                        notGot.getAndIncrement();
                    }
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }finally {
                    tryCount--;
                }
            }
            end.countDown();
        }
    }
}
