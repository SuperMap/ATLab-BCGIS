package com.atlchain.bcgis.storage;

import com.atlchain.bcgis.Utils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

// TODO HBase存储类
@Path("storage/hbase")
public class HBase {

    private  Logger logger = Logger.getLogger(HBase.class.toString());
    private  Admin admin;
    private  Connection connection;
    private  Configuration configuration;
    {
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "192.168.40.156");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Path("/uploadFile")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(
            @FormDataParam("tableName")String tableName,
            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition disposition
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int n = 0;
        while (-1 != (n = inputStream.read(bytes))){
            byteArrayOutputStream.write(bytes,0, n);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String rowKey = String.valueOf(byteArray.hashCode()) +tableName.hashCode();
        HBASEUploadFile(tableName,rowKey,"info",fileExtName,byteArray);
        close(connection,admin);
        result.put("upload_tableName",tableName);
        result.put("upload_rowKey",rowKey);
        result.put("upload_info","info");
        result.put("upload_cn",fileExtName);
        return result.toString();
    }

    @Path("/uploadString")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadString(
            @FormDataParam("tableName")String tableName,
            @FormDataParam("value")String value,
            @FormDataParam("file") FormDataContentDisposition disposition
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        String fileExtName = Utils.getExtName(disposition.getFileName());
        String rowKey = String.valueOf(value.hashCode()) +tableName.hashCode();
        HBASEUploadFile(tableName,rowKey,"info",fileExtName,value);
        close(connection,admin);
        result.put("upload_tableName",tableName);
        result.put("upload_rowKey",rowKey);
        result.put("upload_info","info");
        result.put("upload_cn",fileExtName);
        return result.toString();
    }

    @POST
    @Path("/download")
    public String download(
            @FormDataParam("tableName") String tableName,
            @FormDataParam("rowKey") String rowKey,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException, IOException {
        JSONObject result = new JSONObject();
        String store_LocalPath = "E:\\DemoRecording\\File_storage\\JerseyTest\\HBasetest" + fileExtName;
        byte[] data = HBASEDownloadFile(tableName,rowKey,"info",fileExtName);
        close(connection,admin);
        OutputStream out = new FileOutputStream(store_LocalPath);
        out.write(data);
        result.put("download_tableName",tableName);
        result.put("download_rowKey",rowKey);
        result.put("download_info","info");
        result.put("download_cn",fileExtName);
        result.put("download_localpath",store_LocalPath);
        return result.toString();
    }

    @POST
    @Path("/deleteValue")
    public String delete(
            @FormDataParam("tableName") String tableName,
            @FormDataParam("rowKey")String rowKey,
            @FormDataParam("fileExtName") String fileExtName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        HBASEDeleteFile(tableName, rowKey,"info",fileExtName);
        close(connection,admin);
        result.put("delete_tableName",tableName);
        result.put("delete_rowKey",rowKey);
        result.put("delete_info","info");
        result.put("delete_cn",fileExtName);
        return result.toString();
    }

    @POST
    @Path("/deleteTable")
    public String delete(
            @FormDataParam("tableName") String tableName
    ) throws JSONException {
        JSONObject result = new JSONObject();
        HBASEDeleteTable(tableName);
        close(connection,admin);
        result.put("delete_tableName",tableName);
        return result.toString();
    }

    private void HBASEUploadFile(String tableName,String rowKey,String cf,String cn,byte[] value){
        if(!tableExist(tableName)){
            creatTable(tableName,cf);
            logger.info("create table ,the name is: " + tableName);
        }
        try {
            Table table =  connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(cf),Bytes.toBytes(cn),Bytes.toBytes(ByteBuffer.wrap(value)));
            table.put(put);
            table.close();
        }catch (IOException e){
        }
    }

    private void HBASEUploadFile(String tableName,String rowKey,String cf,String cn,String value){
        if(!tableExist(tableName)){
            creatTable(tableName,cf);
            logger.info("create table ,the name is: " + tableName);
        }
        try {
            Table table =  connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(cf),Bytes.toBytes(cn),Bytes.toBytes(value));
            table.put(put);
            table.close();
        }catch (IOException e){
        }
    }

    private byte[] HBASEDownloadFile(String tableName,String rowKey,String cf,String cn){
        byte[] Value = null;
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            get.addColumn(Bytes.toBytes(cf),Bytes.toBytes(cn));
            Result results = table.get(get);
            Cell[] cells = results.rawCells();
            for(Cell cell:cells){
                Value = CellUtil.cloneValue(cell);
            }
            table.close();
        }catch (IOException e){
        }
        return Value;
    }

    private void HBASEDeleteFile(String tableName,String rowKey,String cf,String cn){
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            delete.addColumns(Bytes.toBytes(cf),Bytes.toBytes(cn));
            table.delete(delete);
            table.close();
        }catch (IOException e){
        }
        logger.info("this table" + tableName + "is delete");
    }

    public void HBASEDeleteTable(String tableName)  {
        try{
            if(!tableExist(tableName)){
                return;
            }
            admin.disableTable(TableName.valueOf(tableName));
            admin.deleteTable(TableName.valueOf(tableName));
        }catch (IOException e){
        }
        logger.info("this table" + tableName + "is delete");
    }

    private boolean tableExist(String tablename){
        boolean tableExists = false ;
        try{
            connection = ConnectionFactory.createConnection(configuration);
            admin = connection.getAdmin();
            tableExists = admin.tableExists(TableName.valueOf(tablename));
        }catch (IOException e){
        }
        return tableExists;
    }

    private void creatTable(String tableName,String info) {
        if(tableExist(tableName)){
            logger.info("this table " + tableName + "is already exist !");
            return;
        }
        HTableDescriptor hTableDescriptor =  new HTableDescriptor(TableName.valueOf(tableName));
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(info);
        hTableDescriptor.addFamily(hColumnDescriptor);
        try {
            admin.createTable(hTableDescriptor);
        }catch (IOException e){
        }
        logger.info("creat "+ tableName +"table is success !");
    }

    private void close(Connection conn,Admin admin){
        if(conn != null){
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(admin != null){
            try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
