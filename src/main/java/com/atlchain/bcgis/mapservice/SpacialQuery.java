package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.data.Shp2Wkb;
import com.atlchain.bcgis.data.Utils;
import com.atlchain.bcgis.data.index.RTreeIndex;
import com.atlchain.bcgis.storage.BlockChain;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 空间分析类
 */
@Path("mapservice/query")
public class SpacialQuery {

    private Logger logger = Logger.getLogger(SpacialQuery.class.toString());
    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());
    public SpacialQuery() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }
    private RTreeIndex rTreeIndex = new RTreeIndex();
    String indexFilePath = "E:\\SuperMapData\\rtree.bin";

    /**
     * 属性查询
     */
    @Path("/attributes")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON) //  MULTIPART_FORM_DATA
    public String attributesAnalysis(
            String params
    ){
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        String attributes = jsonObject.getString("attributes");
        String fid = jsonObject.getString("fid");
        List<String> stringList = new ArrayList<>();
        stringList.add(attributes);
        stringList.add(fid);
        String attributesJSON = queryGeometryByAttributes(stringList);
        System.out.println(attributesJSON);
        return attributesJSON;
    }

    // TODO 空间查询
    @Path("/spatial")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON) //  MULTIPART_FORM_DATA
    public String spatialAnalysis(
            String params
    ){
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        Double minX = Double.valueOf(jsonObject.getString("minX"));
        Double minY = Double.valueOf(jsonObject.getString("minY"));
        Double maxX = Double.valueOf(jsonObject.getString("maxX"));
        Double maxY = Double.valueOf(jsonObject.getString("maxY"));

        String spatialJSON = queryGeometryBySpatial(minX, minY, maxX, maxY);
        System.out.println(spatialJSON);
        return spatialJSON;
    }

    private String queryGeometryByAttributes(List<String> stringList){
        String attributesHash = client.getRecord(
                stringList,
                "bcgiscc",
                "GetAttributesRecordByKey"
        );
        List<String> list = Arrays.asList(attributesHash.split("\n"));
        int stringSize = list.get(0).length();
        int listSize = list.size();
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        Geometry geometry = null;
        for(int i = 0; i < listSize ; i++){
            String S1 = list.get(i);
            String keyA ;
            if(S1.length() == stringSize){
                keyA = S1.substring(10, stringSize);
            }else {
                continue;
            }
            byte[][] result = client.getRecordBytes(
                    keyA,
                    "bcgiscc",
                    "GetRecordByKey"
            );
            try {
                geometry = Utils.getGeometryFromBytes(result[0]);
                geometryArrayList.add(geometry);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String attributesJSON = com.atlchain.bcgis.Utils.geometryTogeometryJSON(geometry);
        return attributesJSON;
    }

    private String queryGeometryBySpatial(Double minX, Double minY, Double maxX, Double maxY) {

        STRtree stRtree = rTreeIndex.loadSTRtree(indexFilePath);
        Geometry searchGeo = rTreeIndex.generateSearchGeo(minX  , minY, maxX, maxY);
        List<Geometry> list = rTreeIndex.query(stRtree, searchGeo);
        Geometry geometryUnio = list.get(0);
        for(Geometry geo : list){
            geometryUnio = geometryUnio.union(geo);
        }
        String spatialJSON = com.atlchain.bcgis.Utils.geometryTogeometryJSON(geometryUnio);
        return spatialJSON;
    }
}
