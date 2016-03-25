package com.ly.thread.producerandconsumer;

/**
 * @author: Administrator
 * Date: 2016/3/24 Time: 14:40
 */
public class Consumer implements Runnable {

    private Plate plate;

    public Consumer(Plate plate) {
        this.plate = plate;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                String s = plate.get();
                System.out.println("第" + s + "个水果取出盘子"+Thread.currentThread().getName());
                Thread.sleep((long) (400 * Math.random()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
