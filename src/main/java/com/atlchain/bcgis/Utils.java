package com.atlchain.bcgis;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
    public static String httpRequest(HttpRequestType type, URI uri) throws IOException {
        return httpRequest(type, uri, null, null, null);
    }

    public static String httpRequest(HttpRequestType type, URI uri, String jsonParams) throws IOException {
        return httpRequest(type, uri, jsonParams, null, null);
    }

    public static String httpRequest(HttpRequestType type, URI uri, String jsonParams, String username, String passwd) throws IOException {
        // 是否有用户名密码
        CloseableHttpClient httpClient = getHttpClient(uri, username, passwd);

        // 发送请求
        int code = 0;
        String result = "";
        CloseableHttpResponse response = null;
        try {
            switch (type) {
                case GET:
                    HttpGet httpGet = new HttpGet(uri + getParamsForGet(jsonParams));
                    httpGet.addHeader("Accept", "application/json");
                    response = httpClient.execute(httpGet);
                    break;
                case POST:
                    HttpPost httpPost = new HttpPost(uri);
                    httpPost.addHeader("Content-Type", "application/json");
                    httpPost.addHeader("Accept", "application/json");
                    StringEntity entity = new StringEntity(jsonParams, "UTF-8");
                    System.out.println(jsonParams);
                    httpPost.setEntity(entity);
//                    getParamsForPost(jsonParams);
                    break;
                default:
                    break;
            }
            code = response.getStatusLine().getStatusCode();
            result = EntityUtils.toString(response.getEntity());
        } finally {
            if (null != response) {
                response.close();
            }
            if (null != httpClient) {
                httpClient.close();
            }
        }
        if (200 != code) {
            return response.getStatusLine().getReasonPhrase() + ", status code: " + code;
        }
        return result;
    }

    private static CloseableHttpClient getHttpClient(URI uri, String username, String passwd) {
        CloseableHttpClient httpClient;
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
        return httpClient;
    }

    private static String getParamsForGet(String jsonString) throws UnsupportedEncodingException {
        if (null == jsonString) {
            return "";
        }
        // 构造 url 参数，并对参数进行编码以防有特殊字符
        JSONObject jsonObject = (JSONObject) JSONObject.parse(jsonString);
        StringBuilder paramsStrBuilder = new StringBuilder();
        boolean firstElement = true;
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (firstElement) {
                paramsStrBuilder.append("?");
                firstElement = false;
            } else {
                paramsStrBuilder.append("&");
            }
            paramsStrBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
            paramsStrBuilder.append("=");
            paramsStrBuilder.append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
        }
        return paramsStrBuilder.toString();
    }

    private static String getParamsForPost(String jsonString) {
        if (null == jsonString) {
            return "";
        }
        // 构造 url 参数，并对参数进行编码以防有特殊字符
        JSONObject jsonObject = (JSONObject) JSONObject.parse(jsonString);
        return null;
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
