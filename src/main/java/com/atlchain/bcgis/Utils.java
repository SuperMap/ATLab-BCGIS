package com.atlchain.bcgis;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * 工具类
 */
public class Utils {

    private static Logger logger = Logger.getLogger(Utils.class.toString());

    /**
     * Http 请求类型
     */
    public enum HttpRequestType {
        GET,
        POST,
        DELETE
    };

    static JSONObject resultJson = new JSONObject();

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

    public static String getSHA256(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(bytes);
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);}
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

    /**
     * 发送 Http 请求
     * @param type GET POST
     * @param uri
     * @return
     * @throws IOException
     */
    public static String httpRequest(HttpRequestType type, URI uri) throws IOException {
        return httpRequest(type, uri, null, null, null);
    }

    public static String httpRequest(HttpRequestType type, URI uri, String jsonParams) throws IOException {
        return httpRequest(type, uri, jsonParams, null, null);
    }

    public static String httpRequest(HttpRequestType type, URI uri, String username, String passwd) throws IOException {
        return httpRequest(type, uri, null, username, passwd);
    }

    public static String httpRequest(HttpRequestType type, URI uri, String jsonParams, String username, String passwd) throws IOException {
        // 是否有用户名密码
        CloseableHttpClient httpClient = getHttpClient(uri, username, passwd);

        if (null != jsonParams && !JSONObject.isValid(jsonParams)) {
            resultJson.put("err", "Bad params format, json format expected!");
            return resultJson.toString();
        }

        // 发送请求
        int code = 0;
        String result = "";
        CloseableHttpResponse response = null;
        try {
            switch (type) {
                case GET:
                    HttpGet httpGet = new HttpGet(uri + getParamsForGet(jsonParams));
                    System.out.println("GET uri: " + uri + getParamsForGet(jsonParams));
                    httpGet.addHeader("Accept", MediaType.APPLICATION_JSON);
                    response = httpClient.execute(httpGet);
                    break;
                case POST:
                    HttpPost httpPost = new HttpPost(uri);
                    httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON);
                    httpPost.addHeader("Accept", MediaType.APPLICATION_JSON);
                    StringEntity entity = new StringEntity(jsonParams, "UTF-8");
                    httpPost.setEntity(entity);
                    response = httpClient.execute(httpPost);
                    break;
                case DELETE:
                    HttpDelete httpDelete = new HttpDelete(uri);
                    httpDelete.addHeader("Accept", MediaType.APPLICATION_JSON);
                    response = httpClient.execute(httpDelete);
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

        resultJson.put("status", code);
        if (200 != code) {
            return response.getStatusLine().getReasonPhrase();
        }
        return result;
    }

    /**
     * 获取 Http Client，主要用于设置用户名密码
     * @param uri
     * @param username
     * @param passwd
     * @return
     */
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

    /**
     * 将 Get 方法的参数拼接到 URL 中
     * @param jsonString
     * @return
     * @throws UnsupportedEncodingException
     */
    private static String getParamsForGet(String jsonString) throws UnsupportedEncodingException {
        if (null == jsonString || "" == jsonString) {
            return "";
        }
        // 构造 url 参数，并对参数进行编码以防有特殊字符
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
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

    public static String geometryTogeometryJSON(Geometry geometry){
        GeometryJSON geometryJSON = new GeometryJSON();
        String stringgeometry = null;
        try{
            StringWriter writer = new StringWriter();
            geometryJSON.write(geometry, writer);
            stringgeometry = writer.toString();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return stringgeometry;
    }

    public static Geometry geometryjsonToGeometry(String JSONstring){
        if(JSONstring.equals(null)){
            logger.info("JSONstring is null ,can not convert to Geometry");
        }
        Geometry geometry = null;
        try {
            GeometryJSON geometryJSON1 = new GeometryJSON();
            Reader reader = new StringReader(JSONstring);
            geometry = geometryJSON1.read(reader);
        }catch (IOException e){
            e.printStackTrace();
        }
        return geometry;
    }

    public static Geometry wkbToGeometry(File file){
        WKBReader reader = new WKBReader();
        Geometry geometry = null;
        try {
            geometry = reader.read(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    public static void geometryToWkbFile(Geometry geometry,File file){
        WKBWriter writer = new WKBWriter();
        byte[] WKBByteArray = writer.write(geometry);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(WKBByteArray);
            out.close();
        } catch (IOException e){
        }
    }

    public static Geometry getGeometryFromBytes(byte[] bytes) throws ParseException {
        Geometry geometry = new WKBReader().read(bytes);
        return geometry;
    }

    public static String inputStreamToString(InputStream inputStream){
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while (true){
            try {
                if (!((line = in.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            buffer.append(line);
        }
        return buffer.toString();
    }

    //hex to byte[]

    public static byte[] hexToByteArray(String inHex){
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1){
            //奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {
            //偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2){
            result[j]=hexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }

    public static byte hexToByte(String inHex){

        return (byte)Integer.parseInt(inHex,16);
    }

    public static void saveFile(InputStream inputStream, String path) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int temp = 0;
            while ((temp = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                Objects.requireNonNull(fileOutputStream).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }


}
