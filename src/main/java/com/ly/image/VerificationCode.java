package com.ly.image;

import org.apache.commons.lang.RandomStringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @author: Administrator
 * Date: 2016/3/18 Time: 11:08
 */
public class VerificationCode {


    public static BufferedImage createImage(String randomCode){
        try {
            //String randomCode = RandomStringUtils.randomAlphanumeric(4);
            //创建图片对象
            int width=75;
            int height=40;
            int imageType=BufferedImage.TYPE_INT_RGB;
            BufferedImage image = new BufferedImage(width, height, imageType);
            //画板(先有笔或刷)
            Graphics g= image.getGraphics();
            g.setColor(Color.WHITE);//设置笔或刷颜色
            g.fillRect(1, 1, width-2, height-2);//画矩形（开始坐标，和画的图的宽度和高度）
            //将随机数画入画板中
            g.setColor(Color.BLACK);//设置笔或刷颜色
            g.setFont(new Font("黑体",Font.BOLD+Font.ITALIC,20));//设置字体(样式，风格，大小)
            // 生成随机数
            Random random = new Random();
            for (int i = 0; i < 50; i++) {
                int xs = random.nextInt(width);
                int ys = random.nextInt(height);
                int xe = xs+random.nextInt(width/8);
                int ye = ys+random.nextInt(height/8);
                int red = random.nextInt(255);
                int green = random.nextInt(255);
                int blue = random.nextInt(255);
                g.setColor(new Color(red, green, blue));
                g.drawLine(xs, ys, xe, ye);
            }
            g.drawString(randomCode, 8, 25);//画字符串（内容，开始坐标）

            return image;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
