package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.atlchain.bcgis.storage.BlockChain;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

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
@Path("mapservice/buffer")
public class SpacialAnalysis {

    private Logger logger = Logger.getLogger(SpacialAnalysis.class.toString());
    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());
    public SpacialAnalysis() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    @Path("/bufferAnalysis")
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
        ArrayList<String> fids = new ArrayList<>();
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                String index = fid.substring(fid.lastIndexOf('.') + 1);
                String strIndex = String.format("%03d", Integer.parseInt(index) - 1);
                fid = "6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4-" + strIndex;
                System.out.println("fid: " + fid);
                fids.add(fid);
            }
        }
        String bufferJSON = buffer(bufferRadius, fids);
        return bufferJSON;
    }

    @Path("/unionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
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
        ArrayList<String> fids = new ArrayList<>();
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                String index = fid.substring(fid.lastIndexOf('.') + 1);
                String strIndex = String.format("%03d", Integer.parseInt(index) - 1);
                fid = "hashDxxx-" + strIndex;
                System.out.println("fid: " + fid);
                fids.add(fid);
            }
        }
        String unionJSON = union(fids);
        return unionJSON;
    }

    @Path("/intersectionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
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
        ArrayList<String> fids = new ArrayList<>();
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                fids.add(fid);
            }
        }
        String intersectionJSON = intersection(fids);
        return intersectionJSON;
    }

    private String buffer(String bufferRadius, List<String> fidList){
        Geometry geometry;
        Geometry geometryUnion ;
        List<Geometry> gemetryList = new LinkedList<>();
        for(String str : fidList){
            geometry = queryGeometryFromChain(str);
            gemetryList.add(geometry);
        }
        geometryUnion = bufferAnalysis(bufferRadius, gemetryList);
        String bufferJSON = Utils.geometryTogeometryJSON(geometryUnion);
        return bufferJSON;
    }

    private Geometry bufferAnalysis(String bufferRadius, List<Geometry> geometryList){
        Geometry geometry ;
        Geometry geometryBuffer = geometryList.get(0).buffer(Double.valueOf(bufferRadius));
        for (Geometry geolist : geometryList) {
            geometry = geolist.buffer(Double.valueOf(bufferRadius));
            geometryBuffer = geometryBuffer.union(geometry);
        }
        return  geometryBuffer;
    }

    private String intersection(List<String> fidList){
        Geometry geometry;
        Geometry geometryUnion ;
        List<Geometry> gemetryList = new LinkedList<>();
        for(String str : fidList){
            geometry = queryGeometryFromChain(str);
            gemetryList.add(geometry);
        }
        geometryUnion = intersectionAnalysis(gemetryList);
        String intersectionJSON = Utils.geometryTogeometryJSON(geometryUnion);
        if(intersectionJSON.equals( "null")){
            logger.info("the selected part without intersection area");
        }
        return intersectionJSON;
    }

    private Geometry intersectionAnalysis(List<Geometry> geometryList){
        Geometry geometry ;
        Geometry geometryIntersection = geometryList.get(0);
        for (Geometry geo : geometryList) {
            geometry = geo;
            geometryIntersection = geometryIntersection.intersection(geometry);
        }
        return geometryIntersection;
    }

    private String union(List<String> fidList){
        Geometry geometry;
        Geometry geometryUnion ;
        List<Geometry> gemetryList = new LinkedList<>();
        for(String str : fidList){
            geometry = queryGeometryFromChain(str);
            gemetryList.add(geometry);
        }
        geometryUnion = unionAnalysis(gemetryList);
        String unionJSON = Utils.geometryTogeometryJSON(geometryUnion);
        return unionJSON;
    }

    private Geometry unionAnalysis(List<Geometry> geometryList){
        Geometry geometry ;
        Geometry geometryUnion = geometryList.get(0);
        for (Geometry geolist : geometryList) {
            geometry = geolist;
            geometryUnion = geometryUnion.union(geometry);
        }
        return  geometryUnion;
    }

    /**
     *  根据FeatureID从区块链上查询数据
     *  只能单独查询，整体查询需要用bcgis里面的getRecord
     * @throws ParseException
     */
    private Geometry queryGeometryFromChain(String featureID) {
        String key = featureID;
        byte[][] result = client.getRecordBytes(
                key,
                "bcgiscc",
                "GetRecordByKey"
        );
        Geometry geometry = null;
        try {
            geometry = Utils.getGeometryFromBytes(result[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }
}