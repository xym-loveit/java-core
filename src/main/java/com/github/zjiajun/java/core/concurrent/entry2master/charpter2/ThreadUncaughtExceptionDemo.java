package com.github.zjiajun.java.core.concurrent.entry2master.charpter2;

/**
 * Created by zhujiajun
 * 16/1/5 19:03
 *
 * 线程unchecked exception例子
 */
public class ThreadUncaughtExceptionDemo implements Runnable {

    @Override
    public void run() {
//        java.lang.ArrayIndexOutOfBoundsException: 1
        Object [] objects = new Object[0];
        System.out.println(objects[1]);
//        java.lang.NumberFormatException: For input string: "FUCK"
//        int number = Integer.parseInt("FUCK");
    }

    public static void main(String[] args) {
        Thread thread = new Thread(new ThreadUncaughtExceptionDemo());
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.err.println("Exception in thread : " + t.getName());
            System.err.println("Stack trace : ");
            e.printStackTrace(System.err);
            System.err.println("Thread status : " + t.getState());
        });
        thread.start();
    }
}
