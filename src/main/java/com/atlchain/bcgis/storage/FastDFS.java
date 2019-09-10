package com.atlchain.bcgis.storage;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;;
import java.io.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.atlchain.bcgis.Utils;

import org.csource.fastdfs.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.json.JSONException;
import org.json.JSONObject;


// TODO FastDFS存储类
@Path("storage/fastdfs")
public class FastDFS {

    Logger logger = Logger.getLogger(FastDFS.class.toString());

    private final String conf_filename = FastDFS.class.getResource("/fdfs_client.conf").getPath();

    /**
     * @Get方式：提供资源的只读访问 根据本地资源的路径将其上传到FastDFS数据库，并返回保存的路径信息
     * @param path
     * @return
     */
    @GET
    @Path("/saveTofastDFS")
    @Produces(MediaType.TEXT_PLAIN)
    public static String put(@QueryParam("path")
                             @DefaultValue("E:\\DemoRecording\\File_storage\\JerseyTest\\\\test1.jpg") String path) {

        byte[] data = new Utils().fileTobyte(path);
        String fileExtName = path.substring(path.lastIndexOf('.')+1) ;

        String[] fileIds = new FastDFS().FastDFSUploadFile( data,fileExtName);
        String GroupID = fileIds[0];
        String filepath = fileIds[1];
        return "=====GroupID: " + GroupID + " =====filepath: " + filepath ;
    }

    @GET
    @Path("/resourceAddress")
    @Produces(MediaType.TEXT_PLAIN)
    public static byte[] putget(@QueryParam("path")
                                @DefaultValue("E:\\DemoRecording\\File_storage\\JerseyTest\\\\test1.jpg") String path){

        byte[] data = new Utils().fileTobyte(path);
        String fileExtName = path.substring(path.lastIndexOf('.')+1) ;
        System.out.println("data" + data.length);
        return data;
    }

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadImg(
                            @FormDataParam("file") InputStream fileInputStream,
                            @FormDataParam("file") FormDataContentDisposition disposition,
                            @FormDataParam("type") String type,
                            @FormDataParam("filename") String resourceId ) throws JSONException, IOException {

        System.out.println("开始调用");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n = 0;
        while ((n = fileInputStream.read(bytes)) != -1){
            byteArrayOutputStream.write(bytes, 0, n);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        JSONObject result = new JSONObject();
        String[] strs = FastDFSUploadFile(byteArray, "jpg");
        result.put("s0", strs[0]);
        result.put("s1", strs[1]);
        System.out.println("结束调用！");
        return result.toString();
    }

    /**
     * @POST方式：用于更新现有资源或者创建一个新资源  读取FastDFS数据库文件并保存到本地设定的路径下
     * 地址 http://localhost:8080/myapp/storage/fastdfs/head
     * @param groupID
     * @param filePath_FastDFS
     * @param savefilePath_Local
     * @return
     */
    @POST
    @Path("/downloadTofastDFS")
    public String ParamPost(@HeaderParam("GroupID")
                            @DefaultValue("group1") String groupID,
                            @HeaderParam("filePath_FastDFS")
                            @DefaultValue("M00/00/00/wKgojV11pm2ELgrMAAAAAGYRQew274.jpg") String filePath_FastDFS,
                            @HeaderParam("savefilePath_Local")
                            @DefaultValue("E:\\DemoRecording\\File_storage\\JerseyTest\\\\test009.jpg")String savefilePath_Local) {

        List<String> downloadlist = new LinkedList<>();
        downloadlist.add(groupID);
        downloadlist.add(filePath_FastDFS);
        downloadlist.add(savefilePath_Local);
        new FastDFS().FastDFSDownloadFile(downloadlist);
        return "=====GroupID: " + groupID + " =====filepath: " + filePath_FastDFS + "====fileExtName ：" + savefilePath_Local;
    }

    /**
     * @DELETE 删除目标下的资源 定位FastDFS数据库中的资源，然后删除
     * @param deleteFileId
     * @param deleteFilepath
     */
    @DELETE
    @Path("deleteTofastDFS")
    public void Paramdelete(@QueryParam("deletefileId")
                            @DefaultValue("group1")String deleteFileId,
                            @QueryParam("deleteFilepath")
                            @DefaultValue("M00/00/00/wKgojV1x8XiEZ1CTAAAAAGYRQew206.jpg")String deleteFilepath ) {

        List<String> deletelist = new LinkedList<>();
        deletelist.add(deleteFileId);
        deletelist.add(deleteFilepath);
        new FastDFS().FastDFSDeleteFile(deletelist);
    }

    //  put file to the FastDFS
    private String[] FastDFSUploadFile(byte[] fileContent, String fileExtName){

        String fileIds[] = new String[2];
        StorageServer storageServer = null;
        TrackerServer trackerServer = null;
        try{
            logger.info("confURL.toString(): \"");
            ClientGlobal.init(conf_filename);
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer,storageServer);
            fileIds = storageClient.upload_appender_file(fileContent,fileExtName,null);
            if(fileIds == null){
                logger.info("DFSUploadFile failed , Because the fileIds is null");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(storageServer != null)
                    storageServer.close();
                if(trackerServer != null)
                    trackerServer.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        logger.info("upload file sucessful!");
        return fileIds ;
    }


    //download file from the FastDFS
    private String FastDFSDownloadFile(List<String> args){

        TrackerServer trackerServer = null;
        StorageServer storageServer = null;
        String groupID = args.get(0);
        String filepath = args.get(1);
        String storePath = args.get(2);

        try{
            ClientGlobal.init(conf_filename);
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer,storageServer);
            byte[] bytes = storageClient.download_file(groupID,filepath);
            if(bytes == null){
                return "DownloadFile is failed";
            }
            OutputStream out = new FileOutputStream(storePath);
            out.write(bytes);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(storageServer!= null)
                    storageServer.close();
                if(trackerServer != null)
                    trackerServer.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        logger.info("download file sucessful!");
        return null;
    }

    // delete file from the FastDFS
    private String FastDFSDeleteFile(List<String> args){
        if (args.size() != 2) {
            return "Incorrect number of arguments. Expecting 2";
        }
        TrackerServer trackerServer = null;
        StorageServer storageServer = null;
        String groupId = args.get(0);
        String Filepath = args.get(1);

        int i = 0 ;
        try{
            ClientGlobal.init(conf_filename);
            TrackerClient tracker = new TrackerClient();
            trackerServer = tracker.getConnection();
            StorageClient storageClient = new StorageClient(trackerServer,storageServer);
            i = storageClient.delete_file(groupId,Filepath);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (storageServer != null)
                    storageServer.close();
                if (trackerServer != null)
                    trackerServer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        logger.info("delete file sucessful!");
        return  null;
    }
}
