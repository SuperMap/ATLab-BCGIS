package com.atlchain.bcgis;

<<<<<<< HEAD
import java.security.MessageDigest;
=======
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
>>>>>>> 8b16c0a97c2225b73771d6440a571d270ea21902

/**
 * 工具类
 */
public class Utils {

    /**
     * Http 请求类型
     */
    public enum HttpRequestType {
        GET,
        POST,
        DELETE
    };

    /**
     * 获取文件后缀名
     * @param fileName 文件名
     * @return
     */
    public static String getExtName(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index, fileName.length());
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    public static String getSHA256(String str) {
        if (str == null) {
            return null;}
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(str.getBytes());
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);}
    }

    public static String httpRequest(HttpRequestType type, URL url, String Authorization, String jsonArgs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod(type.toString());
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json; charset=UTF-8");
        connection.setRequestProperty("Authorization", "Basic " + Authorization);

        OutputStream os = null;
        StringBuilder builder = new StringBuilder();
        try {
            if (HttpRequestType.POST == type) {
                connection.setDoInput(true);
                os = connection.getOutputStream();
                os.write(jsonArgs.getBytes());
                os.flush();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while (null != (line = br.readLine())) {
                builder.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != os) {
                os.close();
            }
            connection.disconnect();
        }
        return builder.toString();
    }
}
