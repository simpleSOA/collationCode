package com.ly.thread.producerandconsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * @author: Administrator
 * Date: 2016/3/24 Time: 14:35
 */
public class Plate {
    private BlockingQueue<String> fruit = new LinkedBlockingDeque<>(10);

    public String get(){
        try {
            return fruit.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void put(String fruitName){
        try {
            fruit.put(fruitName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
