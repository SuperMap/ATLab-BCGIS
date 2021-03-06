package com.atlchain.bcgis.storage;

import com.atlchain.bcgis.Utils;
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
    public void upload() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/uploading";
        String filePath = "E:\\DemoRecording\\testFileStorage\\JerseyTest\\test1.jpg";
        String fileNameExtream = filePath.substring(filePath.lastIndexOf('.')-1, filePath.length());
        File file = new File(filePath);
        try {
            InputStream in = new FileInputStream(file);
            FileInputStream in1 = new FileInputStream(file);
            String hash = Utils.getSHA256(Utils.inputStreamToString(in1));
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addBinaryBody("file", in, ContentType.MULTIPART_FORM_DATA, fileNameExtream);
            entityBuilder.addTextBody("hash", hash );
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
    public void download() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/download";
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("download_Path", "test");
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
    public void delete() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/delete";
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("delete_Path", "test");
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