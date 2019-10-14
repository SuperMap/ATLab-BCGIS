package com.atlchain.bcgis.mapservice;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atlchain.bcgis.Utils;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class SpacialAnalysisTest {

    private static final String BASE_URI = "http://localhost:8899/bcgis/";

    public String httpPost(MultipartEntityBuilder entityBuilder, String url) {
        String result = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entityBuilder.build());


            HttpResponse response = httpClient.execute(httpPost);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, Consts.UTF_8);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String buffer(String JerseyPath, JSONObject jsonObject) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("JSONObject", String.valueOf(jsonObject));
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }

    public String intersection(String JerseyPath, JSONObject jsonObject) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("JSONObject", String.valueOf(jsonObject));
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }


    public String union(String JerseyPath, JSONObject jsonObject) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("JSONObject", String.valueOf(jsonObject));
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }

    // -------------------------------以上为前提，下面进行测试分析----------------------

    /**
     * 测试前需要修改源码部分为 @Consumes(MediaType.MULTIPART_FORM_DATA) 对应的值修改为 @FormDataParam("JSONObject") String params
     */

    // 缓冲区分析
    @Test
    public void testBuffer() {
        File bufferfile = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\bufferD2222.wkb");
        JSONObject jsonObject = new JSONObject();
        JSONArray fid = new JSONArray();
        jsonObject.put("bufferRadius","0.002");
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-0");
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-10");
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-20");
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-38");
        jsonObject.put("fid",fid);
        String stringJSON = buffer("mapservice/buffer/bufferAnalysis",jsonObject);
        System.out.println(stringJSON);
        JSONObject json = JSONObject.parseObject(stringJSON);
        String string = (String) json.get("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-0");
        Geometry bufferGeometry = Utils.geometryjsonToGeometry(string);
        Utils.geometryToWkbFile(bufferGeometry, bufferfile);
    }

    // 联合分析
    @Test
    public void testUnion() {
        Long start = System.nanoTime() / 1000000L;
        File unionfile = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\BL_FeatureIDs_toChain.wkb");
        JSONObject jsonObject = new JSONObject();
        JSONArray fid = new JSONArray();
        for (int i = 0; i < 10; i++) {
            fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-" + i);
            // d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-
            // 6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4-
        }
        jsonObject.put("fid",fid);
        Long start1 = System.nanoTime() / 1000000L;
        String stringJSON = union("mapservice/buffer/unionAnalysis", jsonObject);
        Long end = System.nanoTime() / 1000000L;
        System.out.println("整体分析耗时:" + (end - start) + "ms\n后台分析数据分析耗时:" + (end - start1) + "ms");
        Geometry bufferGeometry = Utils.geometryjsonToGeometry(stringJSON);
        Utils.geometryToWkbFile(bufferGeometry, unionfile);
    }

    // 叠加分析
    @Test
    public void testIntersection() {
        File fileJSON = new File("E:\\DemoRecording\\File_storage\\Test_SpaceAnalysis\\intersectionD_ListFeatureIDs.wkb");
        JSONObject jsonObject = new JSONObject();
        JSONArray fid = new JSONArray();
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-1");
        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-2");
//        fid.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-7");
        jsonObject.put("fid",fid);
        String stringJSON = intersection("mapservice/buffer/intersectionAnalysis", jsonObject);
        Geometry geometryjson = Utils.geometryjsonToGeometry(stringJSON);
        Utils.geometryToWkbFile(geometryjson, fileJSON);
    }
}