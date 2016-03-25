package com.ly.thread.producerandconsumer;

/**
 * @author: Administrator
 * Date: 2016/3/24 Time: 14:43
 */
public class ProducerConsumerTest {
    public static void main(String[] args) {
        Plate p = new Plate();
        Producer producer = new Producer(p);
        Consumer consumer = new Consumer(p);
        new Thread(producer).start();
        new Thread(consumer).start();
        new Thread(consumer).start();
    }
}
