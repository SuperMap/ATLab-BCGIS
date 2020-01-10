package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.atlchain.bcgis.data.protoBuf.protoConvert;
import com.atlchain.bcgis.storage.BlockChain;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 缓冲区分析类
 */
@Path("mapservice/Analysis")
public class SpacialAnalysis {

    private Logger logger = Logger.getLogger(SpacialAnalysis.class.toString());
    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());
    public SpacialAnalysis() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    @Path("/buffer")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String bufferAnalysis(
            String params
    ){
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        String bufferRadius = jsonObject.getString("bufferRadius");
        JSONArray fidJsonArray = jsonObject.getJSONArray("fid");
        List<Integer> indexList = new LinkedList<>();
        String hash = null;
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                int index = Integer.parseInt(fid.substring(fid.lastIndexOf('.') + 1)) - 1;
                if(i == 0){
                    hash = fid.substring(0, fid.lastIndexOf('.'));
                }
                indexList.add(index);
            }
        }
        String bufferJSON = doBuffer(bufferRadius, hash, indexList);
        return bufferJSON;
    }

    @Path("/union")
    @POST
    @Produces(MediaType.APPLICATION_JSON) // MULTIPART_FORM_DATA
    @Consumes(MediaType.APPLICATION_JSON)
    public String unionAnalysis(
            String params
    ){
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        JSONArray fidJsonArray = jsonObject.getJSONArray("fid");
        List<Integer> indexList = new LinkedList<>();
        String hash = null;
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                int index = Integer.parseInt(fid.substring(fid.lastIndexOf('.') + 1)) - 1;
                if(i == 0){
                    hash = fid.substring(0, fid.lastIndexOf('.'));
                }
                indexList.add(index);
            }
        }
        String unionJSON = doUnion(hash, indexList);
        return unionJSON;
    }


    @Path("/intersection")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON) // MULTIPART_FORM_DATA
    public String intersectionAnalysis(
            String params
    ){
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        JSONArray fidJsonArray = jsonObject.getJSONArray("fid");
        List<Integer> indexList = new LinkedList<>();
        String hash = null;
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                int index = Integer.parseInt(fid.substring(fid.lastIndexOf('.') + 1)) - 1;
                if(i == 0){
                    hash = fid.substring(0, fid.lastIndexOf('.'));
                }
                indexList.add(index);
            }
        }
        String intersectionJSON = doIntersection(hash, indexList);
        return intersectionJSON;
    }

    /**
     * 缓冲区分析
     * @param bufferRadius
     * @param hash
     * @param indexList
     * @return
     */
    private String doBuffer(String bufferRadius, String hash, List<Integer> indexList){
        Geometry geoTmp;
        String geometryJSON = null;
        JSONObject bufferJSON = new JSONObject();
        int count = 2 + queryIndexFromChain(hash);
        for(int index : indexList){
            String hashID = hash + "-" + String.format("%0" + count + "d", index);
            geoTmp = queryGeometryFromChain(hashID);
            Geometry geometryBuffer = geoTmp.buffer(Double.valueOf(bufferRadius));
            geometryJSON = Utils.geometryTogeometryJSON(geometryBuffer);
            bufferJSON.put(hashID, geometryJSON);
        }
//        return bufferJSON.toString();
        logger.info("analysis buffer is success");
        return geometryJSON;
    }

    /**
     * 叠加分析
     * @param hash
     * @param indexList
     * @return
     */
    private String doIntersection(String hash, List<Integer> indexList){
        Geometry geoTmp = null;
        int count = 2 + queryIndexFromChain(hash);
        String tmpHash = hash + "-" + String.format("%0" + count + "d", indexList.get(0));
        Geometry geometryIntersection = queryGeometryFromChain(tmpHash);
        for(int index : indexList){
            String hashID = hash + "-" + String.format("%0" + count + "d", index);
            geoTmp = queryGeometryFromChain(hashID);
            geometryIntersection = geometryIntersection.intersection(geoTmp);
        }
        String intersectionJSON = Utils.geometryTogeometryJSON(geometryIntersection);
        if(intersectionJSON.equals( "null")){
            logger.info("the selected part without intersection area");
        } else {
            logger.info("analysis intersection is success");
        }
        return intersectionJSON;
    }

    /**
     * 联合分析
     * @param hash
     * @param indexList
     * @return
     */
    private String doUnion(String hash, List<Integer> indexList){
        Geometry geoTmp;
        int count = 2 + queryIndexFromChain(hash);
        String tmpHash = hash + "-" + String.format("%0" + count + "d", indexList.get(0));
        Geometry geometryUnion = queryGeometryFromChain(tmpHash);
        for(int index : indexList){
            String hashID = hash + "-" + String.format("%0" + count + "d", index);
            geoTmp = queryGeometryFromChain(hashID);
            geometryUnion = geometryUnion.union(geoTmp);
        }
        String unionJSON = Utils.geometryTogeometryJSON(geometryUnion);
        logger.info("analysis union is success");
        return unionJSON;
    }

    /**
     *  根据FeatureID从区块链上查询数据
     * @throws ParseException
     */
    private Geometry queryGeometryFromChain(String featureID) {
        String key = featureID;
        byte[][] result = client.getRecordBytes(
                key,
                "bcgiscc",
                "GetRecordByKey"
        );
//        Geometry geometry = protoConvert.getGeometryFromProto(result[0]);
        Geometry geometry = null;
        try {
            geometry = new WKBReader().read(result[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    /**
     * 根据hash查询整体信息返回整体个数
     * @param hash
     * @return
     */
    private int queryIndexFromChain(String hash){
        int count = 0;
        String result = client.getRecord(
                hash,
                "bcgiscc",
                "GetRecordByKey"
        );

        if(result.length() != 0){
            JSONObject jsonObject = (JSONObject) JSON.parse(result);
            count = (int)jsonObject.get("count");
        } else {
            logger.info("Please enter the correct hash value");
        }
        return String.valueOf(count).length();
    }
}
