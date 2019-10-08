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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URI;
import java.util.logging.Logger;

// TODO HDFS存储类
@Path("storage/hdfs")
public class HDFS {

    private Logger logger = Logger.getLogger(HDFS.class.toString());
    private final String ipAddress = "hdfs://192.168.40.147:9000";
    private FileSystem fs = null;
    private String userName = "java";

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("uploadpath")String uploadpath,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition disposition
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        String upload_location = hdfsUploadFile(inputStream, uploadpath, fileExtName);
        result.put("upload_location", upload_location);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @FormDataParam("download_Path") String download_Path,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        String store_LocalPath = "E:\\DemoRecording\\File_storage\\JerseyTest\\HDFStest" + fileExtName;
        hdfsDownloadFile(store_LocalPath, download_Path, fileExtName);
        result.put("savefilePath_Local", store_LocalPath);
        return result.toString();
    }

    @POST
    @Path("/delete")
    public String delete(
            @FormDataParam("delete_Path") String delete_Path,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        String deletepath = hdfsDeleteFile(delete_Path, fileExtName);
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

    private String hdfsUploadFile(InputStream fileInputStream, String upload_Path, String fileExtName) throws IOException {
        FileSystem fs = new HDFS().getFs();
        String upload_Location = "/user/bcgis/" + upload_Path + fileExtName;
        org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(ipAddress + upload_Location);
        FSDataOutputStream os = fs.create(dst);
        IOUtils.copy(fileInputStream, os);
        fs.close();
        logger.info("sucessful upload file !");
        return  upload_Location;
    }

    private void hdfsDownloadFile(String store_LocalPath, String download_Path, String fileExtName){
        FileSystem fs = new HDFS().getFs();
        download_Path = ipAddress + "/user/bcgis/" + download_Path + fileExtName;
        try {
            fs.copyToLocalFile(new org.apache.hadoop.fs.Path(download_Path),
                    new org.apache.hadoop.fs.Path(store_LocalPath));
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
