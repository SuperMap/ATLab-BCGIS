package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.atlchain.bcgis.storage.BlockChain;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 缓冲区分析类
 */
@Path("mapservice/buffer")
public class BufferAnalysis {

    private Logger logger = Logger.getLogger(BufferAnalysis.class.toString());
    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());

    public BufferAnalysis() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    @Path("/bufferAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String bufferFeatureIDs(
            @FormDataParam("JSONObject") String params
//            String params
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
                fids.add(fid);
            }
        }
        String bufferJSON = buffer(bufferRadius, fids);
        return bufferJSON;
    }

    @Path("/unionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String unionFeatureIDs(
            @FormDataParam("Key") String key,
            @FormDataParam("FeatureIDs") String FeatureIDs
    ){
        String unionJSON ;
        if(!key.equals("union")){
            unionJSON = "please inter the right key ";
        }else {
            unionJSON = union( FeatureIDs);
        }
        return unionJSON;
    }

    @Path("/intersectionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String intersectionJSON(
            @FormDataParam("Key") String key,
            @FormDataParam("FeatureIDs") String FeatureIDs
    ){
        String intersectionJSON ;
        if(!key.equals("intersection")){
            intersectionJSON = "please inter the right key ";
        }else {
            intersectionJSON = intersection(FeatureIDs);
        }
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
//        Geometry geometry = Utils.geometryjsonToGeometry(json);
//        Geometry bufferGeometry = geometry.buffer(Double.valueOf(bufferRadius));
//        String bufferJSON = Utils.geometryTogeometryJSON(bufferGeometry);
        // 即将每次缓冲区分析的值都联合在一起返回即可
        Geometry geometry ;
        Geometry geometryBuffer = geometryList.get(0).buffer(Double.valueOf(bufferRadius));
        for (Geometry geolist : geometryList) {
            geometry = geolist.buffer(Double.valueOf(bufferRadius));
            geometryBuffer = geometryBuffer.union(geometry);
        }
        return  geometryBuffer;
    }

    private String intersection(String FeatureIDs){
        File file = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\D.wkb");
        Geometry geometry = Utils.wkbToGeometry(file);
        List<String> list = new LinkedList<>();
        List<String> listAdd = Arrays.asList(FeatureIDs.split(","));
        for(String str:listAdd){
            list.add(str.trim());
        }
        Geometry geometryIntersection ;
        List<Geometry> gemetryList = new LinkedList<>();
        for(int i=0;i<list.size();i++){
            gemetryList.add(geometry.getGeometryN(Integer.valueOf(list.get(i))));
        }
        geometryIntersection = intersectionAnalysis(gemetryList);
        String unionJSON = Utils.geometryTogeometryJSON(geometryIntersection);
        if(unionJSON.equals( "null")){
            unionJSON = "the selected part without intersection area";
        }
        return unionJSON;
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

    private String union(String FeatureIDs){
        Geometry geometry;
        List<String> list = new LinkedList<>();
        List<String> listAdd = Arrays.asList(FeatureIDs.split(","));
        for(String str:listAdd){
            list.add(str.trim());
        }
        Geometry geometryUnion ;
        List<Geometry> gemetryList = new LinkedList<>();
        for(int i=0;i<list.size();i++){
            geometry = queryGeometryFromChain(list.get(i));
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
