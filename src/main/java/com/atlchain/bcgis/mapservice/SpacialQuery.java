package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.data.Shp2Wkb;
import com.atlchain.bcgis.data.Utils;
import com.atlchain.bcgis.data.index.RTreeIndex;
import com.atlchain.bcgis.data.protoBuf.protoConvert;
import com.atlchain.bcgis.storage.BlockChain;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
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
        String tmpFid = jsonObject.getString("fid");
        String fid = tmpFid.substring(tmpFid.indexOf(":") + 1, tmpFid.length());
        String attributesJSON = null;
        JSONObject jsonPorp = new JSONObject();
        Set<String> keys = jsonObject.keySet();
        if (keys.size() < 2) {
            // 第一次传入参数  返回具体有哪些属性值
            jsonPorp = queryAttributesFromChain(fid);
            attributesJSON = jsonPorp.toJSONString();
        } else {
            // 第二次传入参数 根据前端发送的查询条件后端进行查询 返回geometryString
            for(String key : keys){
                if( ! key.equals("fid")){
                    jsonPorp.put(key, jsonObject.getString(key));
                }
            }
            jsonPorp.put("hashIndex", fid);
            attributesJSON = queryGeometryByAttributes(jsonPorp);
        }
//        System.out.println(attributesJSON);
        return attributesJSON;
    }

    /**
     * 空间查询
     * @param params
     * @return
     */
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
        String typeName = String.valueOf(jsonObject.getString("fid"));
        String fid = typeName.substring(typeName.indexOf(":") + 1, typeName.length());
        String spatialJSON = queryGeometryBySpatial(minX, minY, maxX, maxY, fid);
        return spatialJSON;
    }

    /**
     * 根据查询语句进行属性查询，返回空间几何数据的 GeoString 形式
     * @param json
     * @return
     */
    public String queryGeometryByAttributes(JSONObject json){
        logger.info("默认分页为1000，若无法返回数据，   " +
                "\n(1)请确认查询条件是否正确" +
                "\n(2)使用 queryPropsByPage(JSONObject json, String page) 自定义分页(<1000)，避免数据量过大而返回不成功");
        // 第一步 根据查询的条件设置为 selector 语句
        Set<String> keys = json.keySet();
        StringBuilder qureySelector = new StringBuilder();
        Boolean start = false;
        int count = 1;
        int total = keys.size();
        qureySelector.append("{\"" );
        for(String key : keys){
            if(start){
                qureySelector.append(",");
                qureySelector.append("\"");
            }
            qureySelector.append(key);
            qureySelector.append("\":\"");
            qureySelector.append(json.getString(key));
            if(count < total){
                qureySelector.append("\"");
            }
            start = true;
            count = count + 1;
        }
        qureySelector.append("\"}");
        String selector = qureySelector.toString();
        logger.info("查询语句为：" + selector);
        // 对分页返回的数据进行处理
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        GeometryCollection geometryCollection = null;
        String bookMark = "";
        String queryResultByPage = "null";
        String page = String.valueOf(1000);
        while (true) {
            queryResultByPage = client.getRecordBySeletorByPage(
                    "bcgiscc",
                    "GetRecordBySelectorByPagination",
                    selector,
                    page,
                    bookMark
            );
            JSONArray jsonArray = JSONArray.parseArray(queryResultByPage);
            if(jsonArray.size() == 1){
                break;
            }
            JSONObject tmpJson = (JSONObject) jsonArray.get(jsonArray.size() - 1);
            bookMark = tmpJson.getString("Record");

            // 解析查询得到的信息
            for(int k = 0; k < jsonArray.size() - 1; k++){
                // 3.1 解析信息得到 hash
                JSONObject jsonObject = JSONObject.parseObject(jsonArray.get(k).toString());
                String tmpValue = jsonObject.getString("Record");
                jsonObject = JSONObject.parseObject(tmpValue);
                String hash = jsonObject.getString("hash");
                // 3.2 根据 hash 得到 geometry
                byte[][] re = client.getRecordBytes(
                        hash,
                        "bcgiscc",
                        "GetRecordByKey"
                );
                Geometry geo = null;
                try {
                    geo = new WKBReader().read(re[0]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                geometryArrayList.add(geo);
            }
        }
        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        geometryCollection = Utils.getGeometryCollection(geometries);
        if(geometryCollection == null){
            return null;
        }
        String propJSON = com.atlchain.bcgis.Utils.geometryTogeometryJSON(geometryCollection);
        logger.info("queryprop is success, the total is " + geometryCollection.getNumGeometries() );
        return propJSON;
    }


    /**
     * 空间查询
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return
     */
    private String queryGeometryBySpatial(Double minX, Double minY, Double maxX, Double maxY, String fid) {
        // TODO 此处的前提是在发布的时候就建立了数据库，然后把生成的空间索引R树以hash为名存好，这里直接调用即可
        String indexFilePath = "E:\\SuperMapData\\RtreeData";
        STRtree stRtree = rTreeIndex.loadSTRtree(indexFilePath + File.separator + fid);
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        Geometry searchGeo = rTreeIndex.generateSearchGeo(minX, minY, maxX, maxY);
        List<Geometry> list = rTreeIndex.query(stRtree, searchGeo);
        for(Geometry geo : list){
            geometryArrayList.add(geo);
        }
        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        GeometryCollection geometryCollection = Utils.getGeometryCollection(geometries);
        String spatialJSON = com.atlchain.bcgis.Utils.geometryTogeometryJSON(geometryCollection);
        logger.info("querySpatial is success");
        return spatialJSON;
    }


    /**
     * 根据 hash 查询整体信息返回整体个数
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

    /**
     *  根据 FeatureID 从区块链上查询该数据具有的属性数据
     * @throws ParseException
     */
    private JSONObject queryAttributesFromChain(String featureID) {
        int count = 2 + queryIndexFromChain(featureID);
        String key = "prop" + featureID + "-" + String.format("%0" + count + "d", 0);
        byte[][] result = client.getRecordBytes(
                key,
                "bcgiscc",
                "GetRecordByKey"
        );
        String resultStr = null;
        try {
            resultStr = "[" + new String(result[0], "UTF-8") + "]";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray = JSONArray.parseArray(resultStr);
        JSONObject json = null;
        for (Object obj : jsonArray) {
            json = (JSONObject) obj;
            json.remove("hash");
            json.remove("hashIndex");
            break;
        }
        logger.info("该数据具有的属性信息为：" + json.toJSONString());
        return json;
    }

    /**
     * 根据 hash 从区块链读取完整的数据
     * @return
     */
    public byte[][]  getDataFromChaincode(String hash){
        String result = client.getRecord(
                hash,
                "bcgiscc",
                "GetRecordByKey"
        );
        if(result.length() == 0){
            logger.info("please input correct recordKey");
        }
        JSONObject jsonObject = (JSONObject)JSON.parse(result);
        // 读取时数据的个数匹配
        int count = (int)jsonObject.get("count");
        JSONArray jsonArray = JSONArray.parseArray(jsonObject.get("readRange").toString());
        byte[][] results = client.getRecordByRange(
                hash,
                "bcgiscc",
                jsonArray
        );
        logger.info("getDataFromChaincode is success");
        return results;
    }

    /**
     * 根据 hash 从区块链读取数据，然后返回 属性和空间几何信息 （proto格式）
     * @param hash
     * @return
     */
    public List<Object> getGeometryAndPropFromChain(String hash){
        Geometry geometry = null;
        byte[][] results = getDataFromChaincode(hash);
        JSONObject jsonProp;
        JSONArray jsonArrayProp = new JSONArray();
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        for (byte[] resultByte : results) {
            String resultStr = new String(resultByte);
            JSONArray jsonArray = (JSONArray)JSON.parse(resultStr);
            for (Object obj : jsonArray){
                JSONObject jsonObj = (JSONObject) obj;
                String recordBase64 = (String)jsonObj.get("Record");
                byte[] bytes = Base64.getDecoder().decode(recordBase64);
                geometry = protoConvert.getGeometryFromProto(bytes);
                geometryArrayList.add(geometry);
                jsonProp = protoConvert.getPropFromProto(bytes);
                jsonArrayProp.add(jsonProp);
            }
        }
        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        GeometryCollection geometryCollection = Utils.getGeometryCollection(geometries);
        List<Object> list = new LinkedList<>();
        list.add(geometryCollection);
        list.add(jsonArrayProp);
        return list;
    }

}
