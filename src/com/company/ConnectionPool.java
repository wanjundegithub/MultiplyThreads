package com.company;

import java.sql.Connection;
import java.util.LinkedList;

public class ConnectionPool {
    //父类和子类调用
    private LinkedList<Connection> pool=new LinkedList<>();

    public ConnectionPool(int size) {
        if(size<0){
            return;
        }
        for(int i=0;i<size;i++){
            pool.add(ConnectionDriver.createConnection());
        }
    }

    /**
     * 连接池回收连接并通知其他消费者
     * @param connection
     */
    public void releaseConnection(Connection connection){
        if(connection==null){
            return;
        }
        synchronized (pool){
            pool.addLast(connection);
            pool.notifyAll();
        }
    }

    /**
     * 消费者获取连接并设置超时等待时间
     * @param mills
     * @return
     * @throws InterruptedException
     */
    public Connection fetchConnection(long mills) throws InterruptedException {
        synchronized (pool){
            if(mills<=0){
                while (pool.isEmpty()){
                    pool.wait();
                }
                return pool.removeFirst();
            }else{
                long future=mills+System.currentTimeMillis();
                long remains=mills;
                while(pool.isEmpty()&&remains>0){
                    pool.wait();
                    remains=future-System.currentTimeMillis();
                }
                return pool.isEmpty()?null:pool.removeFirst();
            }
        }
    }
}
