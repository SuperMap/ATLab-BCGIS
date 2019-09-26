package com.atlchain.bcgis;


import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

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

    // Http Get Request
    public static String httpRequest(URI uri) throws IOException {
        return httpRequest(uri, null, null, null);
    }

    public static String httpRequest(URI uri, Map<String, String> params) throws IOException {
        return httpRequest(uri, params, null, null);
    }

    public static String httpRequest(URI uri, Map<String, String> params, String username, String passwd) throws IOException {

        // 是否有用户名密码
        CloseableHttpClient httpClient = null;
        if (null != username && null != passwd ) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(username, passwd)
            );
            httpClient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();
        } else {
            httpClient = HttpClientBuilder.create().build();
        }

        HttpGet httpGet = null;
        if ( null != params) {
            // 构造 url 参数，并对参数进行编码以防有特殊字符
            StringBuilder paramsStr = new StringBuilder();
            boolean firstElement = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (firstElement) {
                    paramsStr.append("?");
                    firstElement = false;
                } else {
                    paramsStr.append("&");
                }
                paramsStr.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                paramsStr.append("=");
                paramsStr.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            }
            httpGet = new HttpGet(uri + paramsStr.toString());
        } else {
            httpGet = new HttpGet(uri);
        }

        httpGet.addHeader("Accept", "application/json");

        // 发送请求
        int code = 0;
        String result = "";
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            code = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
        } finally {
            if (null != response){
                try {
                    response.close();
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (200 != code) {
            return response.getStatusLine().getReasonPhrase() + ", status code: " + code;
        }
        return result;
    }

    // Http Post Request


//    public static String httpRequest(HttpRequestType type, URL url, String Authorization) throws IOException {
//        return Utils.httpRequest(type, url, Authorization, "");
//    }
//
//    public static String httpRequest(HttpRequestType type, URL url, String Authorization, String jsonArgs) throws IOException {
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setDoOutput(true);
//        connection.setRequestMethod(type.toString());
//        connection.setRequestProperty("Content-Type", "application/json");
//        connection.setRequestProperty("Accept", "application/json");
//        connection.setRequestProperty("Authorization", "Basic " + Authorization);
//
//        OutputStream os = null;
//        StringBuilder builder = new StringBuilder();
//        try {
//            if (HttpRequestType.POST == type) {
//                connection.setDoInput(true);
//                os = connection.getOutputStream();
//                os.write(jsonArgs.getBytes());
//                os.flush();
//            }
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
//            String line;
//            while (null != (line = br.readLine())) {
//                builder.append(line + "\n");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (null != os) {
//                os.close();
//            }
//            connection.disconnect();
//        }
//        return builder.toString();
//    }
}
