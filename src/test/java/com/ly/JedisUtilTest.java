package com.ly;

import com.ly.database.redis.JedisUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class JedisUtilTest {

    @Test
    public void hmget(){
        List<String> list = JedisUtil.getInstance().hmget("test", "test1");
        System.out.println(list);
    }

    @Test
    public void mset(){
        JedisUtil.getInstance().mset("test","hello", "test1","world");
        String test = JedisUtil.getInstance().get("test");
        Assert.assertTrue("hello".equals(test));
        String test1 = JedisUtil.getInstance().get("test1");
        Assert.assertTrue("world".equals(test1));
    }
}
