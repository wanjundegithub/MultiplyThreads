package com.company;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

public class ConnectionDriver {
    static class ConnectionHandle implements InvocationHandler{
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(method.getName().equalsIgnoreCase("commit")){
                Thread.sleep(1000);
            }
            System.out.println(Thread.currentThread().getName()+" invoke commit method");
            return null;
        }
    }

    /**
     * 代理生成Connection，在commit时休眠1s
     * @return
     */
    public static Connection createConnection(){
        return (Connection) Proxy.newProxyInstance(ConnectionDriver.class.getClassLoader(),new Class<?>[]{Connection.class},new ConnectionHandle());
    }
}
