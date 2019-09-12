package com.atlchain.bcgis.mapservice;

import com.atlchain.bcgis.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

// TODO WMS服务类
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
//        if (!createWorkspace("testWS")) {
//            return "Error: Cannot create workspace";
//        }
        // 创建数据存储
        createDataStore("testWS", "testDS");
        // 发布图层
        return "successfully";
    }

    @DELETE
    @Path("delete")
    public void delete() {
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
        URL url = null;
        JSONObject jsonObj = null;
        try {
            url = new URL(URI + "/workspaces");
            String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization, "");
            jsonObj = new JSONObject(response);
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
            JSONObject json = new JSONObject();
            json.put("workspace", new JSONObject().put("name", workspaceName));
            String args = json.toString();
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
        try {
            url = new URL(URI + "/workspaces/" + workspaceName + "/datastores");
            String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
            String response = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization, "");
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
            response = Utils.httpRequest(Utils.HttpRequestType.POST, url, Authorization, args);
            result = workspaceName.equals(response.trim());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
