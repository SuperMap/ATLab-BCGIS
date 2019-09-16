package com.atlchain.bcgis;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class UtilsTest {
    final String USERNAME = "admin";
    final String PASSWD = "geoserver";

    @Test
    public void httpGet() throws IOException {
        URL url = new URL("http://localhost:8080/geoserver/rest/workspaces/tiger/datastores/nyc");
        String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
        String result = Utils.httpRequest(Utils.HttpRequestType.GET, url, Authorization, "");
        System.out.println(result);
    }

    @Test
    public void httpPost() throws IOException, JSONException {
        URL url = new URL("http://localhost:8080/geoserver/rest/workspaces/testWS/datastores/testDS/featuretypes");
        String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
        String str = "{\n" +
                "  \"featureType\": {\n" +
                "    \"name\": \"tempfeaturesType\",\n" +
                "    \"nativeName\": \"tempfeaturesType\",\n" +
                "    \"namespace\": {\n" +
                "      \"name\": \"testWS\",\n" +
                "      \"href\": \"http://localhost:8080/geoserver/rest/namespaces/testWS.json\"\n" +
                "    },\n" +
                "    \"title\": \"tempfeaturesType\",\n" +
                "    \"keywords\": {\n" +
                "      \"string\": [\n" +
                "        \"features\",\n" +
                "        \"tempfeaturesType\"\n" +
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
                "      \"name\": \"testWS:testDS\",\n" +
                "      \"href\": \"http://localhost:8080/geoserver/rest/workspaces/testWS/datastores/testDS.json\"\n" +
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
        JSONObject json = new JSONObject(str);
        String args = json.toString();
        String result = Utils.httpRequest(Utils.HttpRequestType.POST, url, Authorization, args);
        System.out.println(result);
    }

    @Test
    public void httpDelete() throws IOException {
        URL url = new URL("http://localhost:8080/geoserver/rest/workspaces/testWS/datastores/testDS/featuretypes/testFT");
        String Authorization = new sun.misc.BASE64Encoder().encode((USERNAME + ":" + PASSWD).getBytes());
        String result = Utils.httpRequest(Utils.HttpRequestType.DELETE, url, Authorization, "");
        System.out.println(result);
    }
}