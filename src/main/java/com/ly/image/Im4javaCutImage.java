package com.ly.image;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import java.io.IOException;

/**
 * im4java 裁图，配合graphicsmagick，效率杠杠的
 * @author: Administrator
 * Date: 2016/3/1 Time: 14:49
 */
public class Im4javaCutImage {

    /**
     * 裁剪图片
     * @param imagePath
     *            源图片路径
     * @param newPath
     *            处理后图片路径
     * @param x
     *            起始X坐标
     * @param y
     *            起始Y坐标
     * @param width
     *            裁剪宽度
     * @param height
     *            裁剪高度
     */
    public static void cutImage(String imagePath, String newPath, int x, int y,
                                int width, int height,double quality)throws InterruptedException, IOException, IM4JavaException {
        IMOperation op = new IMOperation();
        op.addImage(imagePath);
        op.crop(width, height, x, y);
        op.quality(quality);
        op.unsharp(2d, 0.5d, 0.7d);
        op.addImage(newPath);
        ConvertCmd convert = new ConvertCmd(true);
        convert.run(op);
    }
    /**
     * 根据尺寸缩放图片[等比例缩放:参数height为null,按宽度缩放比例缩放;参数width为null,按高度缩放比例缩放]
     * @param imagePath 源图片路径
     * @param newPath   处理后图片路径
     * @param width     缩放后的图片宽度
     * @param height    缩放后的图片高度
     */
    public static void zoomImage(String imagePath, String newPath, Integer width,
                                 Integer height) throws InterruptedException, IOException, IM4JavaException {
        zoomImage(imagePath,newPath,width,height,null);
    }

    public static void zoomImage(String imagePath, String newPath, Integer width,
                                 Integer height,Double quality) throws InterruptedException, IOException, IM4JavaException {
        IMOperation op = new IMOperation();
        op.addImage(imagePath);
        if (width == null) {// 根据高度缩放图片
            op.resize(null, height);
        } else if (height == null) {// 根据宽度缩放图片
            op.resize(width, null);
        } else {
            op.resize(width, height);
        }
        if(quality != null){
            op.quality(quality);
        }
        //op.unsharp(2d, 0.5d, 0.7d);
        op.addImage(newPath);
        ConvertCmd convert = new ConvertCmd(true);
        convert.run(op);
    }
}
