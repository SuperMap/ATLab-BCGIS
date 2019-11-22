package com.atlchain.bcgis.storage;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.util.logging.Logger;

@Path("storage/hdfs")
public class HDFS {

    private Logger logger = Logger.getLogger(HDFS.class.toString());
    private final String ipAddress = "hdfs://172.16.15.65:9000";
    private final String hdfsStorePath = "/user/bcgis/";
    private FileSystem fs = null;
    private String userName = "java";

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition disposition,
            @FormDataParam("hash") String hash
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        String hdfsStoreFileHash = hdfsUploadFile(inputStream, fileExtName, hash);
        result.put("hdfsStoreFileHash", hdfsStoreFileHash);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @FormDataParam("downloadPath") String downloadPath
    ) throws JSONException {
        JSONObject result = new JSONObject();
        String fileExtName = downloadPath.substring(downloadPath.lastIndexOf('.'), downloadPath.length());
        String storeLocalPath = "E:\\DemoRecording\\testFileStorage\\JerseyTest\\HDFS_Downloadtest" + fileExtName;
        hdfsDownloadFile(storeLocalPath, downloadPath);
        result.put("savefilePathLocal", storeLocalPath);
        return result.toString();
    }

    @POST
    @Path("/delete")
    public String delete(
            @FormDataParam("deletePath") String deletePath,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        String deletepath = hdfsDeleteFile(deletePath, fileExtName);
        result.put("the delete file path is", deletepath);
        return result.toString();
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

    private String hdfsUploadFile(InputStream fileInputStream, String fileExtName, String hash) throws IOException {
       FileSystem fs = new HDFS().getFs();
        hash = Utils.getSHA256(hash);
        System.out.println("hash:" + hash );
        String uploadPath = hdfsStorePath + hash + fileExtName;
        String destpath = ipAddress + uploadPath;
        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(destpath);
        FSDataOutputStream os = fs.create(dst);
        IOUtils.copy(fileInputStream, os);
        fs.close();
        logger.info("sucessful upload file !");
        return  hash + fileExtName;
    }

    private void hdfsDownloadFile(String storeLocalPath, String downloadPath){
        FileSystem fs = new HDFS().getFs();
        downloadPath = ipAddress + hdfsStorePath + downloadPath;
        try {
            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(downloadPath), new org.apache.hadoop.fs.Path(storeLocalPath));
            fs.close();
        }catch(Exception e){
        }
        logger.info("sucessful download file !");
    }

    private String hdfsDeleteFile(String delete_Path, String fileExtName){
        FileSystem fs = new HDFS().getFs();
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
