package com.atlchain.bcgis.CooperativeStorage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.atlchain.bcgis.storage.BlockChain;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
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
import java.net.URISyntaxException;

public class BlockChainAndHdfs {

    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());

    public BlockChainAndHdfs() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    /**
     * 存入 ：将 s3m 数据存入到 hdfs 数据库，然后返回路径，将路径存入区块链，返回 hash  存入到hdfs数据库的文档值为文件流的hash独一无二，这个可内置别人就没法修改
     * 读取 ：根据 hash 从区块链中获得路径，然后根据路径从 hdfs 获取数据保存到本地
     * 问题：文件夹的可操作性，需要保证不能被删除，或者说这个数据的独一无二的，不带特征才可以不删除（除开删库）
     */
    @Test
    public void testHdfs2Chaincode(){
        // 将本地数据存入 hdfs
        String result = hdfsUpload();
        System.out.println(result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String path = jsonObject.getString("hdfsStoreFileHash");
        // 存入区块链
        String key = "hdfs";
        client.putRecord(
                key,
                result,
                "bcgiscc",
                "PutRecordBytes"
        );
        // 从区块链获取 hash
        String value = client.getRecord(key, "bcgiscc", "GetRecordByKey");
        System.out.println("value:" + value);
        // 读取数据
        hdfsDownload(path);
        System.out.println("");
    }

    public String hdfsUpload() {
        String url = "http://localhost:8899/bcgis/storage/hdfs/uploading";
        String filePath = "E:\\DemoRecording\\testFileStorage\\JerseyTest\\lou1.s3m"; // test1.jpg   dianshitai.s3m
        String fileNameExtream = filePath.substring(filePath.lastIndexOf('.')-1, filePath.length());
        File file = new File(filePath);
        String result="";
        try {
            FileInputStream in = new FileInputStream(file);
            FileInputStream in1 = new FileInputStream(file);
            String hash = Utils.getSHA256(Utils.inputStreamToString(in1));
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addBinaryBody("file", in, ContentType.MULTIPART_FORM_DATA, fileNameExtream);
            entityBuilder.addTextBody("hash", hash );
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entityBuilder.build());
            HttpResponse response = httpClient.execute(httpPost);
            if (response!=null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void hdfsDownload(String path) {
        String url = "http://localhost:8899/bcgis/storage/hdfs/download";
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.addTextBody("downloadPath", path);
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
