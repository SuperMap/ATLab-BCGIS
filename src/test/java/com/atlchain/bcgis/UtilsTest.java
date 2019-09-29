package com.atlchain.bcgis;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

public class UtilsTest {
    final String USERNAME = "admin";
    final String PASSWD = "passwd";

    @Test
    public void httpGet() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("workspaceName", "D");
        jsonObject.put("datastoreName", "D");
        String jsonParams = jsonObject.toJSONString();
        String result = "";
        try {
            result = Utils.httpRequest(Utils.HttpRequestType.GET, URI.create("http://localhost:8899/bcgis/mapservice/wms/list"), jsonParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        Assert.assertNotEquals("", result);
    }

    @Test
    public void httpPost() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("workspaceName", "testWS");
        jsonObject.put("datastoreName", "testDS");
        jsonObject.put("featuretypeName", "testFT");
        String jsonParams = jsonObject.toJSONString();
        String result = "";
        try {
            result = Utils.httpRequest(Utils.HttpRequestType.POST, URI.create("http://localhost:8899/bcgis/mapservice/wms/publish"), jsonParams, USERNAME, PASSWD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("result: " + result);
        Assert.assertNotEquals("", result);
    }

    @Test
    public void httpDelete() throws IOException, JSONException {
        URI uri = URI.create("http://localhost:8899/bcgis/mapservice/wms/delete");
        JSONObject json = new JSONObject();
        json.put("workspaceName", "testWS");
        json.put("layerName", "testFT");
        json.put("datastoreName", "testDS");
        json.put("featuretypeName", "testFT");
        String args = json.toString();
        String result = Utils.httpRequest(Utils.HttpRequestType.POST, uri, args, USERNAME, PASSWD);
        System.out.println(result);
    }

//    @Test
//    public void testHttp() {
//        String url = "http://localhost:8899/bcgis/mapservice/wms/publish";
//        try {
//            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
//            entityBuilder.addTextBody("workspaceName", "workspaceName");
//            entityBuilder.addTextBody("datastoreName", "datastoreName");
//            entityBuilder.addTextBody("featuretypeName", "featuretypeName");
//            CloseableHttpClient httpClient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setEntity(entityBuilder.build());
//            HttpResponse response = httpClient.execute(httpPost);
//            String result="";
//            if (response!=null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                HttpEntity resEntity = response.getEntity();
//                if(resEntity != null){
//                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
//                }
//            }
//            System.out.println(result);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}