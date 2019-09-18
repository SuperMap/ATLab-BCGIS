package com.atlchain.bcgis.storage;

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

public class FastDFSTest {

    @Test
    public void upload() {
        String url = "http://localhost:8899/bcgis/storage/fastdfs/uploading";
        String filePath = "E:\\DemoRecording\\File_storage\\JerseyTest\\test2.jpg";
        File file = new File(filePath);
        try {
            InputStream in = new FileInputStream(file);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("type", "image");
            entityBuilder.addTextBody("filename", "test1.jpg");
            System.out.println("Client："+in.available());
            entityBuilder.addBinaryBody("file", in, ContentType.MULTIPART_FORM_DATA, "234.jpg");
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