package com.ly.io;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件压缩工具
 */
public class FileCompressUtil {

    /**
     * 把多个文件压缩成zip格式
     * @param files       需要压缩的文件
     * @param zipFilePath 压缩后的zip文件路径 ,如"D:/test/aa.zip";
     */
    public static void compressFiles2Zip(File[] files, String zipFilePath) throws IOException {
        if ((files != null) && (files.length > 0)) {
            if (isEndsWithZip(zipFilePath)) {

                File zipFile = new File(zipFilePath);
                try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(zipFile)) {
                    zaos.setUseZip64(Zip64Mode.AsNeeded);
                    for (File file : files) {
                        if (file.exists()) {
                            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file, file.getName());
                            zaos.putArchiveEntry(zipArchiveEntry);
                            try (InputStream is = new FileInputStream(file)) {
                                byte[] buffer = new byte[1024 * 5];
                                int len;
                                while ((len = is.read(buffer)) != -1) {
                                    zaos.write(buffer, 0, len);
                                }
                                zaos.closeArchiveEntry();
                            }
                        }
                    }
                    zaos.finish();
                }
            }
        }
    }
    /**
     * 把单个文件压缩成zip格式
     * @param file       需要压缩的文件，如："D:/test/aa.txt"；
     * @param zipFilePath 压缩后的zip文件路径 ,如"D:/test/aa.zip";
     */
    public static void compressFiles2Zip(File file, String zipFilePath) throws IOException{
        File[] files = {file};
        compressFiles2Zip(files,zipFilePath);
    }


    /**
     * 判断文件名是否以.zip为后缀
     *
     * @param fileName 需要判断的文件名
     * @return 是zip文件返回true, 否则返回false
     */
    public static boolean isEndsWithZip(String fileName) {
        boolean flag = false;
        if (fileName != null && !"".equals(fileName.trim())) {
            if (fileName.endsWith(".ZIP") || fileName.endsWith(".zip")) {
                flag = true;
            }
        }
        return flag;
    }

}

