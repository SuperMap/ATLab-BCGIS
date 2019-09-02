package com.atlchain.bcgis.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.type.Name;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class BCGISDataStore extends ContentDataStore {

    Logger logger = Logger.getLogger(BCGISDataStore.class.toString());

    private File networkConfigFile;
    private String chaincodeName;
    private String functionName;
    private String recordKey;

    public BCGISDataStore(
            File networkConfigFile,
            String chaincodeName,
            String functionName,
            String recordKey
    )
    {
        this.networkConfigFile = networkConfigFile;
        this.chaincodeName = chaincodeName;
        this.functionName = functionName;
        this.recordKey = recordKey;
    }

    // only use in the test :
    public String putDataOnBlockchain(File shpFile) throws IOException, InterruptedException {
        String fileName = shpFile.getName();
        String ext = Files.getFileExtension(fileName);
        if(!"shp".equals(ext)) {
            throw new IOException("Only accept shp file");
        }

        String result = "";
        BlockChainClient client = new BlockChainClient(networkConfigFile);

        Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);
        ArrayList<Geometry> geometryArrayList = shp2WKB.getGeometry();
        String geometryStr = Utils.getGeometryStr(geometryArrayList);
        String hash = Utils.getSHA256(geometryStr);
        String key = hash;
        System.out.println(key);
        String mapname = fileName.substring(0, fileName.lastIndexOf("."));

        JSONObject argsJson = new JSONObject();
        argsJson.put("mapname", mapname);
        argsJson.put("count", geometryArrayList.size());
        argsJson.put("hash", key);
        argsJson.put("geotype", geometryArrayList.get(0).getGeometryType());
        argsJson.put("PID", "");
        String args = argsJson.toJSONString();
        result = client.putRecord(
                key,
                args,
                chaincodeName,
                "PutRecord"
        );
        if (!result.contains("successfully")) {
            return "Put data on chain FAILED! MESSAGE:" + result;
        }

        int index = 0;
        for (Geometry geo : geometryArrayList) {
            byte[] geoBytes = Utils.getBytesFromGeometry(geo);
            String recordKey = key + "-" + index;
            result = client.putRecord(
                    recordKey,
                    geoBytes,
                    chaincodeName,
                    "PutRecordBytes"
            );
            index++;
            if (!result.contains("successfully")) {
                return "Put data on chain FAILED! MESSAGE:" + result;
            }
            //            Thread.sleep(1000);
        }
        return result;
    }

    protected Geometry getRecord(){
        logger.info("===================>getRecord========" +System.currentTimeMillis());

        BlockChainClient client = new BlockChainClient(networkConfigFile);

        String result = client.getRecord(
                this.recordKey,
                this.chaincodeName,
                this.functionName
        );

        JSONObject jsonObject = (JSONObject)JSON.parse(result);
        int count = (int)jsonObject.get("count");

        Geometry geometry = null;
        byte[][] results = client.getRecordByRange(
                this.recordKey,
                this.chaincodeName
        );

        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        for (byte[] resultByte : results) {
            String resultStr = new String(resultByte);
            JSONArray jsonArray = (JSONArray)JSON.parse(resultStr);
            if (count != jsonArray.size()) {
                return null;
            }
            for (Object obj : jsonArray){
                JSONObject jsonObj = (JSONObject) obj;
                String recordBase64 = (String)jsonObj.get("Record");
                byte[] bytes = Base64.getDecoder().decode(recordBase64);
                try {
                    geometry = new WKBReader().read(bytes);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                geometryArrayList.add(geometry);
            }
        }
        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        GeometryCollection geometryCollection = Utils.getGeometryCollection(geometries);

        if (geometryArrayList == null) {
            try {
                throw new IOException("Blockchain record is not available");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("===============================>" + geometryCollection.getNumGeometries());
        return geometryCollection;
    }

    @Override
    protected List<Name> createTypeNames() {
        // TODO 查询所有安装的链码，返回链码名列表。以链码名为 typenames。
        // List<String>  chaincodes = getChaincodeList();
        // return chaincodes;
        // 暂时以一个固定的名字作为 TypeName
        return Collections.singletonList(getTypeName());
    }

    Name getTypeName(){
        String string = "featuresType" ;
        return new NameImpl(string);

    }

    protected ContentFeatureSource getFeatureSource() throws IOException {
        ContentEntry entry = ensureEntry(getTypeName());
        return new BCGISFeatureStore(entry,Query.ALL);

    }

    @Override
    public ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        logger.info("===================>createFeatureSource==================" +  entry.getTypeName());
        return new BCGISFeatureStore(entry, Query.ALL);
    }
}










