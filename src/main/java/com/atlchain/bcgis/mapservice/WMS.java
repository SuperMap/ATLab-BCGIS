package com.atlchain.bcgis.mapservice;

import com.atlchain.bcgis.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

@Path("mapservice/wms")
public class WMS {
    private Logger logger = Logger.getLogger(WMS.class.getName());
    private final String URI = "http://localhost:8080/geoserver/rest";
    private final String USERNAME = "admin";
    private final String PASSWD = "geoserver";

    @POST
    @Path("publish")
    public String publish() {
        // 创建工作区
        if (!createWorkspace("testWS")) {
            logger.info("Cannot create workspace. It is already exist or something wrong happened, please check the logs.");
        }
        // 创建数据存储
        if (!createDataStore("testWS", "testDS")) {
            logger.info("Cannot create dataStore. It is already exist or something wrong happened, please check the logs.");
        }
        // 发布图层
        if (!createFeatureTypes("testWS", "testDS", "testFT")) {
            logger.info("Cannot create featureType. It is already exist or something wrong happened, please check the logs.");
        }

        return "successfully";
    }

    @DELETE
    @Path("delete")
    public void delete() {
        if(!deleteFeatureTypes("testWS","testFT" , "testDS", "testFT")) {
            logger.info("Cannot delete layer. It is already exist or something wrong happened, please check the logs.");
        }
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
            URL url = new URL(URI + "/workspaces");
            String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());

            // 判断工作区是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization);
            JSONObject jsonObj = new JSONObject(response);
            Object objWorkspaces = jsonObj.get("workspaces");
            if (!("".equals(objWorkspaces))) {
                JSONObject jsonWorkspaces = (JSONObject)objWorkspaces;
                JSONArray jsonWorkspace = (JSONArray) jsonWorkspaces.get("workspace");
                for (int i = 0; i < jsonWorkspace.length(); i++) {
                    JSONObject obj = (JSONObject) jsonWorkspace.get(i);
                    if (workspaceName.equals(obj.get("name"))) {
                        logger.info(workspaceName + " is already exist!");
                        return false;
                    }
                }
            }

            // 创建工作区的Json配置
            JSONObject json = new JSONObject();
            json.put("workspace", new JSONObject().put("name", workspaceName));
            String args = json.toString();

            // 创建工作区
            response = Utils.httpRequest(Utils.HttpRequestType.POST, url, Authorization, args);
            result = workspaceName.equals(response.trim());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        logger.info("workspace " + workspaceName + " create successfully!");
        return result;
    }

    private boolean createDataStore(String workspaceName, String datastore) {
        if (null == datastore || null == workspaceName) {
            return false;
        }

        boolean result = false;
        URL url = null;
        JSONObject jsonObj = null;

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
        try {
            url = new URL(URI + "/workspaces/" + workspaceName + "/datastores");
            String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());

            // 判断数据存储是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization);
            jsonObj = new JSONObject(response);
            Object objDatastores = jsonObj.get("dataStores");
            if (!("".equals(objDatastores))) {
                JSONObject jsonDatastores = (JSONObject) objDatastores;
                JSONArray jsonDatastore = (JSONArray) jsonDatastores.get("dataStore");
                for (int i = 0; i < jsonDatastore.length(); i++) {
                    JSONObject obj = (JSONObject) jsonDatastore.get(i);
                    if (datastore.equals(obj.get("name"))) {
                        logger.info(datastore + " is already exist!");
                        return false;
                    }
                }
            }
            JSONObject json = new JSONObject();
            json.put("datastore", new JSONObject().put("name", workspaceName));

            response = Utils.httpRequest(Utils.HttpRequestType.POST, url, Authorization, args);
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
        String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());

        // 创建属性类型的Json配置
        String str = "{\n" +
                "  \"featureType\": {\n" +
                "    \"name\": \""+featureTypeName+"\",\n" +
                "    \"nativeName\": \"tempfeaturesType\",\n" +
                "    \"namespace\": {\n" +
                "      \"name\": \"" + workspaceName + "\",\n" +
                "      \"href\": \"http://localhost:8080/geoserver/rest/namespaces/" + workspaceName + ".json\"\n" +
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
                "      \"href\": \"http://localhost:8080/geoserver/rest/workspaces/" + workspaceName + "/datastores/" + datastore + ".json\"\n" +
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
        JSONObject json = null;
        try {
            URL url = new URL("http://localhost:8080/geoserver/rest/workspaces/" + workspaceName + "/datastores/" + datastore + "/featuretypes");

            // 判断属性类型是否已存在
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization);
            JSONObject jsonObj = new JSONObject(response);
            Object objFeatureTypes = jsonObj.get("featureTypes");
            if (!("".equals(objFeatureTypes))) {
                JSONObject jsonFeatureTypes = (JSONObject) objFeatureTypes;
                JSONArray jsonFeatureType = (JSONArray) jsonFeatureTypes.get("featureType");
                for (int i = 0; i < jsonFeatureType.length(); i++) {
                    JSONObject obj = (JSONObject) jsonFeatureType.get(i);
                    if (featureTypeName.equals(obj.get("name"))) {
                        logger.info( "FeatureType \"" + featureTypeName + "\" is already exist!");
                        return false;
                    }
                }
            }

            // 创建属性类型
            json = new JSONObject(str);
            String args = json.toString();
            response = Utils.httpRequest(Utils.HttpRequestType.POST, url, Authorization, args);
            result = "".equals(response.trim());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean deleteFeatureTypes(String workspaceName, String layername, String datastore, String featureTypeName) {
        String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
        try {
            String response = "";
            URL layerUrl = new URL("http://localhost:8080/geoserver/rest/workspaces/" + workspaceName + "/layers/" + layername);
            response = Utils.httpRequest(Utils.HttpRequestType.DELETE, layerUrl, Authorization);
            if (!("".equals(response))) {
                logger.warning("Cannot not delete layer " + layername);
                return false;
            }
            URL featureUrl = new URL("http://localhost:8080/geoserver/rest/workspaces/" + workspaceName + "/datastores/" + datastore + "/featuretypes/" + featureTypeName);
            response = Utils.httpRequest(Utils.HttpRequestType.DELETE, featureUrl, Authorization);
            if (!("".equals(response))) {
                logger.warning("Cannot not delete featureType " + featureTypeName);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
