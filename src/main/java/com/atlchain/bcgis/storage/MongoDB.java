package com.atlchain.bcgis.storage;

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
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Logger;

// TODO MongoDB存储类
@Path("storage/mongodb")
public class MongoDB {

    private Logger logger = Logger.getLogger(FastDFS.class.toString());

    @Path("/uploading")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String upload(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName")String collectionName,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition disposition)
            throws IOException, JSONException {
        JSONObject result = new JSONObject();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n = 0;
        while (-1 != (n = fileInputStream.read(bytes))){
            byteArrayOutputStream.write(bytes, 0, n);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        String ID = MongoDBUploadFile(databaseName,collectionName,fileExtName,byteArray);
        result.put("ID", ID);
        result.put("fileExtName",fileExtName);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName")String collectionName,
            @FormDataParam("ID") String ID,
            @FormDataParam("fileExtName") String fileExtName
    ){
        String localpath = "E:\\DemoRecording\\File_storage\\JerseyTest\\MongoDBtest" + fileExtName;
        MongoDBDownloadFile(databaseName,collectionName,ID,localpath);
        return "savefilePath_Local : " + localpath;
    }

    @POST
    @Path("/delete")
    public String delete(
            @FormDataParam("databaseName") String databaseName,
            @FormDataParam("collectionName")String collectionName,
            @FormDataParam("ID") String ID
    ) {
        MongoDBDeleteFile(databaseName,collectionName,ID);
        return "the delete file id is :" + ID;
    }


    private String MongoDBUploadFile(String databaseName ,String collectionName,String fileExtName ,byte[] dataByte){

        final String ID = Utils.getSHA256(dataByte.toString());

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        Document document = new Document("title", "MongoDB")
                .append("fileExtName", fileExtName)
                .append("ID",ID)
                .append(ID,dataByte);
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        collection.insertMany(documents);
        mongoClient.close();
        logger.info("UploadFile successfully");
        return  ID ;
    }

    private void MongoDBDownloadFile(String databaseName ,String collectionName,String downloadID ,String localStorePathName){
        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            FindIterable<Document> findIterable = collection.find();
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            while (mongoCursor.hasNext()) {
                Document doc1 = mongoCursor.next();
                Binary doc2 = (Binary) doc1.get(downloadID);
                byte[] data1 = doc2.getData();
            Files.write(Paths.get(localStorePathName), data1);
            }
            mongoClient.close();
            logger.info("DownloadFile successfully and the store path is :" + localStorePathName);
        }catch (Exception e){
        }
    }

    private void MongoDBDeleteFile(String databaseName ,String collectionName,String deleteID){

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

        collection.deleteMany(Filters.eq("ID",deleteID));
        logger.info("The delete file id is : " +deleteID);
        mongoClient.close();
    }
}
