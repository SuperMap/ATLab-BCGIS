package com.atlchain.bcgis;

/**
 * 工具类
 */
public class Utils {
    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return
     */
    public static String getExtName(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index, fileName.length()-1);
    }
}
