package com.atlchain.bcgis.storage;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import io.ipfs.api.MerkleNode;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@Path("storage/cooperativeStorage")
public class CooperativeStorage {

    private Logger logger = Logger.getLogger(HDFS.class.toString());
    private final String ipAddress = "hdfs://172.16.15.65:9000";
    private final String hdfsStorePath = "/user/bcgis/";
    private FileSystem fs = null;
    private String userName = "java";

    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());
    public CooperativeStorage() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    @Path("/test")
    @POST
    @Produces(MediaType.APPLICATION_JSON)        //  返回
    @Consumes(MediaType.APPLICATION_JSON)       //   接收   APPLICATION_JSON  MULTIPART_FORM_DATA
    public String upload(
            String params
    ) throws IOException, URISyntaxException {
        JSONObject jsonObject = JSONObject.parseObject(params);
        String fileExtName = "." + jsonObject.getString("fileNameExtream");
        String data = jsonObject.getString("data");
        String saveFileName = Utils.getSHA256(data);
        byte[] byteData = Utils.hexToByteArray(data);
        // 将数据保存到本地
//        OutputStream os = new FileOutputStream("E:\\SuperMapData\\" + saveFileName  + fileNameExtream);
//        os.write(byteData, 0, byteData.length);
//        os.flush();
//        os.close();
        // 数据存入到 hdfs
        InputStream inputStream = new ByteArrayInputStream(byteData);
        hdfsUploadFile(inputStream, fileExtName, saveFileName);
        // 数据存入到区块链
        // 存入区块链
        String key = "hdfs";
        client.putRecord(
                key,
                saveFileName,
                "bcgiscc",
                "PutRecordBytes"
        );
        return "save file success";
    }

    private FileSystem getFs() {
        Configuration conf = new Configuration();
        conf.set("bcgis",ipAddress);
        conf.set("dfs.replication", "3");
        try {
            fs = FileSystem.get(new URI(ipAddress), conf, userName);
        }catch (Exception e){
        }
        return fs;
    }

    private String hdfsUploadFile(InputStream fileInputStream, String fileExtName, String hash) throws IOException, URISyntaxException {
        FileSystem fs = new CooperativeStorage().getFs();
        String uploadPath = hdfsStorePath + hash + fileExtName;
        String destpath = ipAddress + uploadPath;
        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(destpath);
        FSDataOutputStream os = fs.create(dst);
        IOUtils.copy(fileInputStream, os);
        fs.close();
        logger.info("sucessful upload file !");
        return  hash + fileExtName;
    }

    private void hdfsDownloadFile(String storeLocalPath, String downloadPath) throws URISyntaxException {
        FileSystem fs = new CooperativeStorage().getFs();
        downloadPath = ipAddress + hdfsStorePath + downloadPath;
        try {
            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(downloadPath), new org.apache.hadoop.fs.Path(storeLocalPath));
            fs.close();
        }catch(Exception e){
        }
        logger.info("sucessful download file !");
    }

    private String hdfsDeleteFile(String delete_Path, String fileExtName) throws URISyntaxException {
        FileSystem fs = new CooperativeStorage().getFs();
        delete_Path = "/user/bcgis/"+ delete_Path + fileExtName;
        try {
            fs.deleteOnExit(new org.apache.hadoop.fs.Path(delete_Path));
            fs.close();
        }catch (Exception e){
        }
        logger.info("sucessful delete file");
        return delete_Path;
    }
}
