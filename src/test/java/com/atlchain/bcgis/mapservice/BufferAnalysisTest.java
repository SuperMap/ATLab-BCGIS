package com.atlchain.bcgis.mapservice;

import com.atlchain.bcgis.Utils;
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
import org.locationtech.jts.io.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class BufferAnalysisTest {

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

    public String buffer(String JerseyPath, String JSONS, String bufferRadius) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("bufferRadius", bufferRadius);
        entityBuilder.addTextBody("JSON", JSONS);
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }

    public String intersection(String JerseyPath, String FeatureIDs, String key) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("Key", key);
        entityBuilder.addTextBody("FeatureIDs", FeatureIDs);
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }

    public String union(String JerseyPath, String ListFeatureIDs, String key) {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("Key", key);
        entityBuilder.addTextBody("FeatureIDs", ListFeatureIDs);
        String url = BASE_URI + JerseyPath;
        String result = httpPost(entityBuilder, url);
        return result;
    }

    // -------------------------------以上为前提，下面进行测试分析----------------------

    // 缓冲区分析
    @Test
    public void testBuffer() {
        File file = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\D.wkb");
        File bufferfile = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\bufferD1.wkb");
        Geometry geometry = Utils.wkbToGeometry(file);
        String gemetryJSON = Utils.geometryTogeometryJSON(geometry.getGeometryN(30));
        String bufferRadius = "0.01";
        String stringJSON = buffer("mapservice/buffer/bufferAnalysis", gemetryJSON, bufferRadius);
        Geometry bufferGeometry = Utils.geometryjsonToGeometry(stringJSON);
        Utils.geometryToWkbFile(bufferGeometry, bufferfile);
    }

    // 联合分析
    @Test
    public void testUnion() {
        Long start = System.nanoTime() / 1000000L;
        File unionfile = new File("E:\\DemoRecording\\testFileStorage\\Test_SpaceAnalysis\\BL_FeatureIDs_toChain.wkb");
        List<String> list = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            list.add("d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885-"+ i);
            // d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885
            // 6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4
        }
        String ListFeatureID = list.toString();
        String ListFeatureIDs = ListFeatureID.substring(1, ListFeatureID.length() - 1);
        Long start1 = System.nanoTime() / 1000000L;
        String stringJSON = union("mapservice/buffer/unionAnalysis", ListFeatureIDs, "union");
        Long end = System.nanoTime() / 1000000L;
        System.out.println("整体分析耗时:" + (end - start) + "ms\n后台分析数据分析耗时:" + (end - start1) + "ms");
        Geometry bufferGeometry = Utils.geometryjsonToGeometry(stringJSON);
        Utils.geometryToWkbFile(bufferGeometry, unionfile);
    }

    // 叠加分析
    @Test
    public void testIntersection() {
        File fileJSON = new File("E:\\DemoRecording\\File_storage\\Test_SpaceAnalysis\\intersectionD_ListFeatureIDs.wkb");
        List<String> list = new LinkedList<>();
        list.add("1");
        list.add("2");
//        list.add("7");
        String ListFeatureID = list.toString();
        String ListFeatureIDs = ListFeatureID.substring(1, ListFeatureID.length() - 1);
        String stringJSON = intersection("mapservice/buffer/intersectionAnalysis", ListFeatureIDs, "intersection");
        Geometry geometryjson = Utils.geometryjsonToGeometry(stringJSON);
        Utils.geometryToWkbFile(geometryjson, fileJSON);
    }
}