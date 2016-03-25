package com.ly.thread.producerandconsumer;

/**
 * @author: Administrator
 * Date: 2016/3/24 Time: 14:40
 */
public class Producer implements Runnable {

    private Plate plate;

    public Producer(Plate plate) {
        this.plate = plate;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                this.plate.put("" + i);
                System.out.println("第" + i + "个水果放入盘子");
                Thread.sleep((long) (200 * Math.random()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
