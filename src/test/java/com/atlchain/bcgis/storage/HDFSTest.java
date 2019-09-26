package com.atlchain.bcgis.storage;

import com.squareup.okhttp.MultipartBuilder;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.*;

public class HDFSTest {

    @Test
    public  static void upload() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/uploading";
        String filePath = "E:\\DemoRecording\\File_storage\\JerseyTest\\test1.jpg";
        File file = new File(filePath);
        try {
            InputStream in = new FileInputStream(file);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addBinaryBody("file", in, ContentType.MULTIPART_FORM_DATA, ".jpg");
            entityBuilder.addTextBody("uploadpath", "testHDFS_test");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entityBuilder.build());
            HttpResponse response = httpClient.execute(httpPost);
            String result="";
            if (response!=null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
                }
            }
            System.out.println(result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  static void download() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/download";
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("download_Path", "testHDFS_test");
            entityBuilder.addTextBody("fileExtName",".jpg");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entityBuilder.build());
            HttpResponse response = httpClient.execute(httpPost);
            String result="";
            if (response!=null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
                }
            }
            System.out.println(result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  static void delete() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/delete";
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("delete_Path", "testHDFS_test");
            entityBuilder.addTextBody("fileExtName",".jpg");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entityBuilder.build());
            HttpResponse response = httpClient.execute(httpPost);
            String result="";
            if (response!=null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
                }
            }
            System.out.println(result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}