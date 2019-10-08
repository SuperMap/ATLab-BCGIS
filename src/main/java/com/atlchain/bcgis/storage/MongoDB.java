package com.atlchain.bcgis.storage;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.Binary;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// TODO MongoDB存储类
@Path("storage/mongodb")
public class MongoDB {

    private Logger logger = Logger.getLogger(FastDFS.class.toString());
    private String mongodbIp = "localhost";
    private int mongodbPort = 27017;

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName") String collectionName,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition disposition
    )throws IOException, JSONException {
        JSONObject result = new JSONObject();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n = 0;
        while (-1 != (n = fileInputStream.read(bytes))){
            byteArrayOutputStream.write(bytes, 0, n);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        String ID = mongodbUploadFile(databaseName, collectionName, fileExtName, byteArray);
        result.put("ID", ID);
        result.put("fileExtName", fileExtName);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName")String collectionName,
            @FormDataParam("ID") String ID,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        String localpath = "E:\\DemoRecording\\File_storage\\JerseyTest\\MongoDBtest" + fileExtName;
        mongodbDownloadFile(databaseName, collectionName, ID, localpath);
        result.put("savefilePath_Local", localpath);
        return result.toString();
    }

    @POST
    @Path("/delete")
    public String delete(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName")String collectionName,
            @FormDataParam("ID") String ID
    ) throws JSONException {
        JSONObject result = new JSONObject();
        mongodbDeleteFile(databaseName,collectionName, ID);
        result.put("the delete file id is", ID);
        return result.toString();
    }

    private String mongodbUploadFile(String databaseName, String collectionName, String fileExtName, byte[] dataByte){
        final String ID = Utils.getSHA256(dataByte.toString());
        MongoClient mongoClient = new MongoClient(mongodbIp, mongodbPort);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document document = new Document("title", "MongoDB")
                                  .append("fileExtName", fileExtName)
                                  .append("ID", ID)
                                  .append(ID, dataByte);
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        collection.insertMany(documents);
        mongoClient.close();
        logger.info("UploadFile successfully");
        return  ID ;
    }

    private void mongodbDownloadFile(String databaseName, String collectionName, String downloadID, String localStorePathName){
        try {
            MongoClient mongoClient = new MongoClient(mongodbIp, mongodbPort);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
            FindIterable<Document> findIterable = collection.find();
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            while (mongoCursor.hasNext()) {
                Document document = mongoCursor.next();
                Binary binary = (Binary) document.get(downloadID);
                byte[] data1 = binary.getData();
            Files.write(Paths.get(localStorePathName), data1);
            }
            mongoClient.close();
            logger.info("DownloadFile successfully and the store path is :" + localStorePathName);
        }catch (Exception e){
        }
    }

    private void mongodbDeleteFile(String databaseName, String collectionName, String deleteID){
        MongoClient mongoClient = new MongoClient(mongodbIp, mongodbPort);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.deleteMany(Filters.eq("ID", deleteID));
        logger.info("The delete file id is : " + deleteID);
        mongoClient.close();
    }
}
