package com.atlchain.bcgis.storage;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.logging.Logger;

// TODO ipfs存储类
@Path("storage/ipfs")
public class Ipfs {

    private Logger logger = Logger.getLogger(Ipfs.class.toString());
    private IPFS ipfs = new IPFS("/ip4/192.168.40.166/tcp/5001");

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition disposition
    )throws JSONException {

        MerkleNode merkleNode = ipfsUploadFile(fileInputStream,disposition.getFileName());

        JSONObject result = new JSONObject();
        JSONObject jsonObject = JSONObject.parseObject(merkleNode.toJSONString());
        String hash = jsonObject.getString("Hash");
        String fileName = jsonObject.getString("Name");
        result.put("Hash", hash);
        result.put("Name", fileName);
        return result.toString();
    }

    @POST
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String download(
            @FormDataParam("hashID") String hashID,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException, IOException {

        byte[] downloadByte = ipfsDownloadFile(hashID);

        JSONObject result = new JSONObject();
        String localpath = "E:\\DemoRecording\\testFileStorage\\JerseyTest\\ipfsDownloadTest" + fileExtName;
        OutputStream out = new FileOutputStream(localpath);
        out.write(downloadByte);
        out.close();
        result.put("saveFilePath_Local", localpath);
        return result.toString();
    }

    private MerkleNode ipfsUploadFile(InputStream fileInputStream, String fileName){
        ipfsInit();
        OutputStream os;
        File file = new File(fileName);
        try {
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];
            while ((len = fileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
            fileInputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        NamedStreamable.FileWrapper fileAdd = new NamedStreamable.FileWrapper(file);
        MerkleNode Result = null;
        try {
            Result = ipfs.add(fileAdd).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result;
    }

    private byte[] ipfsDownloadFile(String hash){
        ipfsInit();
        byte[] downloadFileContents = null;
        Multihash downloadFile = Multihash.fromBase58(hash);
        try {
            downloadFileContents = ipfs.cat(downloadFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadFileContents;
    }

    private void ipfsInit(){
        try {
            ipfs.refs.local();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}