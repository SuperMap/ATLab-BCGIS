package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

@Path("mapservice/wms")
public class WMS {
    private Logger logger = Logger.getLogger(WMS.class.getName());
    private final String geoserverURI = "http://localhost:8070/geoserver/rest";
    private final String USERNAME = "admin";
    private final String PASSWD = "geoserver";

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public String list(
            @QueryParam("workspaceName") String workspaceName,
            @QueryParam("datastoreName") String datastoreName
    ) {
        URI uri = URI.create(geoserverURI + "/workspaces/" + workspaceName + "/datastores/" + datastoreName);
        System.out.println(uri);
        String result = null;
        try {
            result = Utils.httpRequest(Utils.HttpRequestType.GET, uri, USERNAME, PASSWD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @POST
    @Path("publish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String publish(
            String params
    ) {
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }
        JSONObject jsonObject = JSONObject.parseObject(params);
        String workspaceName = jsonObject.getString("workspaceName");
        String datastoreName = jsonObject.getString("datastoreName");
        String featuretypeName = jsonObject.getString("featuretypeName");

        // 创建工作区
        if (!createWorkspace(workspaceName)) {
            logger.warning("Cannot create workspace. It is already exist or something wrong happened, please check the logs.");
        }
        // 创建数据存储
        if (!createDataStore(workspaceName, datastoreName)) {
            logger.info("Cannot create dataStore. It is already exist or something wrong happened, please check the logs.");
        }
        // 发布图层
        if (!createFeatureTypes(workspaceName, datastoreName, featuretypeName)) {
            logger.info("Cannot create featureType. It is already exist or something wrong happened, please check the logs.");
        }
        result.put("result", "successfully!");
        return result.toString();
    }

    @POST
    @Path("delete")
    @Consumes("application/json")
    @Produces("application/json")
    public String delete(
            String params
    ) {
        JSONObject result = new JSONObject();
        if (!JSONObject.isValid(params)) {
            result.put("result", "Bad params format, json format expected!");
            return result.toString();
        }

        if(!deleteFeatureTypes(params)) {
            logger.info("Cannot delete layer. It is already exist or something wrong happened, please check the logs.");
        }

        result.put("result", "successfully!");
        return result.toString();
    }

    /**
     * 创建工作空间，如果已存在则跳过
     * @param workspaceName 工作空间名
     * @return
     */
    private boolean createWorkspace(String workspaceName) {
        if (null == workspaceName) {
            return false;
        }
        boolean result = false;
        try {
            URI uri = URI.create(geoserverURI + "/workspaces");

            // 判断工作区是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, uri, USERNAME, PASSWD);
            if (!JSONObject.isValid(response)) {
                logger.warning("response: " + response);
                return false;
            }
            JSONObject jsonObj = JSONObject.parseObject(response);
            JSONObject jsonWorkspaces = jsonObj.getJSONObject("workspaces");
            if (!jsonWorkspaces.isEmpty()) {
                JSONArray jsonWorkspace = jsonWorkspaces.getJSONArray("workspace");
                if (!jsonWorkspace.isEmpty()) {
                    for (int i = 0; i < jsonWorkspace.size(); i++) {
                        JSONObject obj = jsonWorkspace.getJSONObject(i);
                        if (workspaceName.equals(obj.getString("name"))) {
                            logger.warning(workspaceName + " is already exist!");
                            return false;
                        }
                    }
                }
            }

            // 创建工作区的Json配置
            // {"workspace":{"name":"testWS"}}
            JSONObject jsonName = new JSONObject();
            jsonName.put("name", workspaceName);
            JSONObject workspaceJson = new JSONObject();
            workspaceJson.put("workspace", jsonName);
            String args = workspaceJson.toString();

            System.out.println(args);
            // 创建工作区
            response = Utils.httpRequest(Utils.HttpRequestType.POST, uri, args, USERNAME, PASSWD);
            System.out.println("response...." + response);
            result = workspaceName.equals(response.trim());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (result) {
            logger.info("workspace " + workspaceName + " create successfully!");
        }
        return result;
    }

    /**
     * 创建数据存储，创建之前
     * @param workspaceName
     * @param datastore
     * @return
     */
    private boolean createDataStore(String workspaceName, String datastore) {
        if (null == datastore || null == workspaceName) {
            return false;
        }

        boolean result = false;
        URI uri = null;
        JSONObject jsonObj = null;
        try {
            uri = URI.create(geoserverURI + "/workspaces/" + workspaceName + "/datastores");

            // 判断数据存储是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, uri, USERNAME, PASSWD);
            if (!JSONObject.isValid(response)) {
                return false;
            }
            jsonObj = JSONObject.parseObject(response);
            JSONObject objDatastores = jsonObj.getJSONObject("dataStores");
            if (null != objDatastores) {
                JSONArray jsonDatastore = objDatastores.getJSONArray("dataStore");
                for (int i = 0; i < jsonDatastore.size(); i++) {
                    JSONObject obj = jsonDatastore.getJSONObject(i);
                    if (datastore.equals(obj.get("name"))) {
                        logger.warning(datastore + " is already exist!");
                        return false;
                    }
                }
            }

            // 创建数据存储的Json配置
            String args = "{\n" +
                    "            \"dataStore\": {\n" +
                    "            \"name\": \"testDS\",\n" +
                    "                    \"connectionParameters\": {\n" +
                    "                    \"entry\": [\n" +
                    "                        {\"@key\":\"config\",\"$\":\"file:network-config-test.yaml\"},\n" +
                    "                        {\"@key\":\"chaincodeName\",\"$\":\"bcgiscc\"},\n" +
                    "                        {\"@key\":\"functionName\",\"$\":\"GetRecordByKey\"},\n" +
                    "                        {\"@key\":\"recordKey\",\"$\":\"6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4\"},\n" +
                    "                        {\"@key\":\"dbtype\",\"$\":\"bcgis\"}\n" +
                    "                    ]\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }";
            // TODO 待构造参数
            // {"datastore":{"name":"testWS"}}
//            JSONObject jsonName = new JSONObject();
//            jsonName.put("name", datastore);
//            JSONObject datastoreJson = new JSONObject();
//            datastoreJson.put("datastore", args);
//            args = datastoreJson.toString();
//            System.out.println(args );

            response = Utils.httpRequest(Utils.HttpRequestType.POST, uri, args, USERNAME, PASSWD);
            result = workspaceName.equals(response.trim());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean createFeatureTypes(String workspaceName, String datastore, String featureTypeName) {
        if (null == datastore || null == workspaceName || null == featureTypeName) {
            return false;
        }

        boolean result = false;
        JSONObject json = null;
        try {
            URI uri = URI.create(geoserverURI + "/workspaces/" + workspaceName + "/datastores/" + datastore + "/featuretypes");
            // 判断属性类型是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, uri, USERNAME, PASSWD);
            if (!JSONObject.isValid(response)) {
                return false;
            }
            JSONObject jsonObj = JSONObject.parseObject(response);
            JSONObject objFeatureTypes = jsonObj.getJSONObject("featureTypes");
            if (null != objFeatureTypes) {
                JSONArray jsonFeatureType = objFeatureTypes.getJSONArray("featureType");
                for (int i = 0; i < jsonFeatureType.size(); i++) {
                    JSONObject obj = jsonFeatureType.getJSONObject(i);
                    if (featureTypeName.equals(obj.get("name"))) {
                        logger.info( "FeatureType \"" + featureTypeName + "\" is already exist!");
                        return false;
                    }
                }
            }

            // 创建属性类型的Json配置
            // TODO 待构造参数
            String str = "{\n" +
                    "  \"featureType\": {\n" +
                    "    \"name\": \""+featureTypeName+"\",\n" +
                    "    \"nativeName\": \"tempfeaturesType\",\n" +
                    "    \"namespace\": {\n" +
                    "      \"name\": \"" + workspaceName + "\",\n" +
                    "      \"href\": \"http://localhost:8070/geoserver/rest/namespaces/" + workspaceName + ".json\"\n" +
                    "    },\n" +
                    "    \"title\": \"" + featureTypeName + "\",\n" +
                    "    \"keywords\": {\n" +
                    "      \"string\": [\n" +
                    "        \"features\",\n" +
                    "        \"" + featureTypeName + "\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"nativeCRS\": \"GEOGCS[\\\"WGS 84\\\", \\n  DATUM[\\\"World Geodetic System 1984\\\", \\n    SPHEROID[\\\"WGS 84\\\", 6378137.0, 298.257223563, AUTHORITY[\\\"EPSG\\\",\\\"7030\\\"]], \\n    AUTHORITY[\\\"EPSG\\\",\\\"6326\\\"]], \\n  PRIMEM[\\\"Greenwich\\\", 0.0, AUTHORITY[\\\"EPSG\\\",\\\"8901\\\"]], \\n  UNIT[\\\"degree\\\", 0.017453292519943295], \\n  AXIS[\\\"Geodetic longitude\\\", EAST], \\n  AXIS[\\\"Geodetic latitude\\\", NORTH], \\n  AUTHORITY[\\\"EPSG\\\",\\\"4326\\\"]]\",\n" +
                    "    \"srs\": \"EPSG:4326\",\n" +
                    "    \"nativeBoundingBox\": {\n" +
                    "      \"minx\": 115.375,\n" +
                    "      \"maxx\": 117.5,\n" +
                    "      \"miny\": 39.416666666667,\n" +
                    "      \"maxy\": 41.083333333333,\n" +
                    "      \"crs\": \"EPSG:4326\"\n" +
                    "    },\n" +
                    "    \"latLonBoundingBox\": {\n" +
                    "      \"minx\": 115.375,\n" +
                    "      \"maxx\": 117.5,\n" +
                    "      \"miny\": 39.416666666667,\n" +
                    "      \"maxy\": 41.083333333333,\n" +
                    "      \"crs\": \"EPSG:4326\"\n" +
                    "    },\n" +
                    "    \"projectionPolicy\": \"FORCE_DECLARED\",\n" +
                    "    \"enabled\": true,\n" +
                    "    \"store\": {\n" +
                    "      \"@class\": \"dataStore\",\n" +
                    "      \"name\": \"" + workspaceName + ":" + datastore + "\",\n" +
                    "      \"href\": \"http://localhost:8070/geoserver/rest/workspaces/" + workspaceName + "/datastores/" + datastore + ".json\"\n" +
                    "    },\n" +
                    "    \"serviceConfiguration\": false,\n" +
                    "    \"maxFeatures\": 0,\n" +
                    "    \"numDecimals\": 0,\n" +
                    "    \"padWithZeros\": false,\n" +
                    "    \"forcedDecimal\": false,\n" +
                    "    \"overridingServiceSRS\": false,\n" +
                    "    \"skipNumberMatched\": false,\n" +
                    "    \"circularArcPresent\": false,\n" +
                    "    \"attributes\": {\n" +
                    "      \"attribute\": {\n" +
                    "        \"name\": \"geom\",\n" +
                    "        \"minOccurs\": 0,\n" +
                    "        \"maxOccurs\": 1,\n" +
                    "        \"nillable\": true,\n" +
                    "        \"binding\": \"org.locationtech.jts.geom.Polygon\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
//            json = new JSONObject(str);
//            String args = json.toString();

            response = Utils.httpRequest(Utils.HttpRequestType.POST, uri, str, USERNAME, PASSWD);
            result = "".equals(response.trim());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean deleteFeatureTypes(String params) {
        JSONObject jsonObject = JSONObject.parseObject(params);
        String workspaceName = jsonObject.getString("workspaceName");
        String layerName = jsonObject.getString("layerName");
        String datastoreName = jsonObject.getString("datastoreName");
        String featuretypeName = jsonObject.getString("featuretypeName");

        try {
            String response = "";
            URI layersUri = URI.create(geoserverURI + "/workspaces/" + workspaceName + "/layers/" + layerName);
            response = Utils.httpRequest(Utils.HttpRequestType.DELETE, layersUri, USERNAME, PASSWD);
            if (null != response) {
                logger.warning("Cannot not delete, layer " + layerName + "is not exist!");
                return false;
            }
            URI uri = URI.create(geoserverURI + "/workspaces/" + workspaceName + "/datastores/" + datastoreName + "/featuretypes" + featuretypeName);

            response = Utils.httpRequest(Utils.HttpRequestType.DELETE, uri,USERNAME, PASSWD);
            if (!("".equals(response))) {
                logger.warning("Cannot not delete featureType " + featuretypeName);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
