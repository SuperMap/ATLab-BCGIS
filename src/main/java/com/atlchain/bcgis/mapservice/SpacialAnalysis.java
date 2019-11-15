package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.atlchain.bcgis.storage.BlockChain;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
                String strIndex = String.format("%05d", Integer.parseInt(index) - 1);
                String hash = fid.substring(0, fid.lastIndexOf('.'));
                fid = hash + "-" + strIndex;
                System.out.println("fid: " + fid);
                fids.add(fid);
            }
        }
        String bufferJSON = doBuffer(bufferRadius, fids);
        return bufferJSON;
    }

    @Path("/unionAnalysis")
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
        ArrayList<String> fids = new ArrayList<>();
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                String index = fid.substring(fid.lastIndexOf('.') + 1);
                String strIndex = String.format("%05d", Integer.parseInt(index) - 1);
                String hash = fid.substring(0, fid.lastIndexOf('.'));
                fid = hash + "-" + strIndex;
                System.out.println("fid: " + fid);
                fids.add(fid);
            }
        }
        String unionJSON = doUnion(fids);
        return unionJSON;
    }

    @Path("/intersectionAnalysis")
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
        ArrayList<String> fids = new ArrayList<>();
        if (!fidJsonArray.isEmpty()) {
            for (int i = 0; i < fidJsonArray.size(); i++) {
                String fid = fidJsonArray.getString(i);
                String index = fid.substring(fid.lastIndexOf('.') + 1);
                String strIndex = String.format("%05d", Integer.parseInt(index) - 1);
                String hash = fid.substring(0, fid.lastIndexOf('.'));
                fid = hash + "-" + strIndex;
                System.out.println("fid: " + fid);
                fids.add(fid);
            }
        }
        String intersectionJSON = doIntersection(fids);
        return intersectionJSON;
    }

    private String doBuffer(String bufferRadius, List<String> fidList){
        Geometry geometryTmp;
        String geometryJSON = null;
        JSONObject bufferJSON = new JSONObject();
        for(String str : fidList){
            geometryTmp = queryGeometryFromChain(str);
            Geometry geometryBuffer = geometryTmp.buffer(Double.valueOf(bufferRadius));
            geometryJSON = Utils.geometryTogeometryJSON(geometryBuffer);
            bufferJSON.put(str,geometryJSON);
        }
//        return bufferJSON.toString();
        return geometryJSON;
    }

    private String doIntersection(List<String> fidList){
        Geometry geometryTmp = null;
        Geometry geometryIntersection = queryGeometryFromChain(fidList.get(1));

        for(String str : fidList){
            geometryTmp = queryGeometryFromChain(str);
            geometryIntersection = geometryIntersection.intersection(geometryTmp);
        }
        String intersectionJSON = Utils.geometryTogeometryJSON(geometryIntersection);
        if(intersectionJSON.equals( "null")){
            logger.info("the selected part without intersection area");
        }
        return intersectionJSON;
    }

    private String doUnion(List<String> fidList){
        Geometry geometryTmp;
        Geometry geometryUnion = queryGeometryFromChain(fidList.get(0));
        for(String str : fidList){
            geometryTmp = queryGeometryFromChain(str);
            geometryUnion = geometryUnion.union(geometryTmp);
        }
        String unionJSON = Utils.geometryTogeometryJSON(geometryUnion);
        return unionJSON;
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
