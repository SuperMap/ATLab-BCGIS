package com.atlchain.bcgis.data;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.io.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Shp2WkbTest {
    private String shpURL = this.getClass().getResource("/Line/Line.shp").getFile();
    private File shpFile = new File(shpURL);
    private Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);
    private BlockChainClient client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());


    public Shp2WkbTest() throws URISyntaxException {
        client = new BlockChainClient(networkFile);
    }

    @Test
    public void testGetRightGeometryCollectionType() {
        try {
            Assert.assertEquals(GeometryCollection.class, shp2WKB.getGeometry().getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetRightGeometryValue() {
        try {
            ArrayList<Geometry> geometryArrayList = shp2WKB.getGeometry();
//            for(Geometry geom : geometryArrayList) {
//                System.out.println(geom);
//            }
            Assert.assertNotNull(geometryArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveWKB(){
        try {
            String path = "/home/cy/Documents/ATL/data/testShapfile/iDesktop/Line/Line.wkb";
            shp2WKB.save(new File(path));
            Assert.assertTrue(Files.exists(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveGeometryToChain() throws IOException {
        String key =  "Line2";
        byte[] bytes = shp2WKB.getGeometryBytes();

        String result = client.putRecord(
                key,
                bytes,
                "bcgiscc",
                "PutByteArray"
        );
        System.out.println(result);
    }

    @Test
    public void testQueryGeometryFromChain() throws ParseException {
        String key = "Line";
        byte[][] result = client.getRecord(
                key,
                "bcgiscc",
                "GetRecordByKey"
        );

        Geometry geometry = Utils.getGeometryFromBytes(result[0]);
        System.out.println(geometry);
    }

}