package com.atlchain.bcgis.storage;

import com.atlchain.bcgis.Utils;
import org.csource.fastdfs.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.logging.Logger;

/**
 * FastDFS 操作类
 */
@Path("storage/fastdfs")
public class FastDFS {

    private Logger logger = Logger.getLogger(FastDFS.class.toString());
    private final String conf_filename = FastDFS.class.getResource("/fdfs_client.conf").getPath();

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition disposition
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n = 0;
        while (-1 != (n = fileInputStream.read(bytes))){
            byteArrayOutputStream.write(bytes, 0, n);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String[] strs = FastDFSUploadFile(byteArray, Utils.getExtName(disposition.getFileName()));
        result.put("GroupID", strs[0]);
        result.put("FilePath", strs[1]);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @HeaderParam("GroupID") String groupID,
            @HeaderParam("filePath_FastDFS") String filePath_FastDFS,
            @HeaderParam("savefilePath_Local")
            @DefaultValue("E:\\DemoRecording\\File_storage\\JerseyTest\\FastDFStest.jpg")String savefilePath_Local
    ) throws JSONException {
        JSONObject result = new JSONObject();
        FastDFSDownloadFile(groupID, filePath_FastDFS, savefilePath_Local);
        result.put("GroupID",groupID);
        result.put("filepath",filePath_FastDFS);
        result.put("savefilePath_Local",savefilePath_Local);
        return result.toString();
    }

    @DELETE
    @Path("delete")
    public String delete(
            @QueryParam("deletefileId") String deleteFileId,
            @QueryParam("deleteFilepath") String deleteFilepath
    ) throws JSONException {
        JSONObject result = new JSONObject();
        FastDFSDeleteFile(deleteFileId, deleteFilepath);
        result.put("the deleteFilepath is",deleteFilepath);
        return result.toString();
    }

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

    private String FastDFSDownloadFile(String groupID, String filepath, String storePath){
        TrackerServer trackerServer = null;
        StorageServer storageServer = null;
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

    private String FastDFSDeleteFile(String groupId, String Filepath){
        TrackerServer trackerServer = null;
        StorageServer storageServer = null;
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
                e.printStackTrace();
            }
        }
        logger.info("delete file sucessful!");
        return null;
    }
}
