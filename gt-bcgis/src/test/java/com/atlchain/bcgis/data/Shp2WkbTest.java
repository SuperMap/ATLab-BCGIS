package com.atlchain.bcgis.data;

import com.atlchain.sdk.ATLChain;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shp2WkbTest {
    private File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
    private File skFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());
    private String shpURL = this.getClass().getResource("/Line/Line.shp").getFile();
    private File shpFile = new File(shpURL);
    private Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);
    private BlockChainClient client = new BlockChainClient(
            certFile,
            skFile,
            "TestOrgA",
            "grpc://172.16.15.66:7051",
            "TestOrgA",
            "admin",
            "OrdererTestOrgA",
            "grpc://172.16.15.66:7050"
    );

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
            GeometryCollection geometryCollection = shp2WKB.getGeometry();
//            for (int i = 0; i < geometryCollection.getNumGeometries(); i++){
//                System.out.println(geometryCollection.getGeometryN(i));
//            }
            Assert.assertNotNull(geometryCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveWKB(){
        try {
            String path = "/home/cy/Documents/ATL/data/50m_cultural/ne_50m_urban_areas.wkb";
            shp2WKB.save(new File(path));
            Assert.assertTrue(Files.exists(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveGeometryToChain() throws IOException {
        WKBWriter wkbWriter = new WKBWriter();
        String key =  "LineWrite5";
        GeometryCollection geometryCollection = shp2WKB.getGeometry();
        byte[] bytes = wkbWriter.write(geometryCollection);

        String result = client.putRecord(
                key,
                bytes,
                "atlchannel",
                "bincc",
                "PutByteArray"
        );
        System.out.println(result);
    }

    @Test
    public void testQueryGeometryFromChain() throws ParseException {
        String key = "LineWrite6";
        byte[][] result = client.getRecord(
                key,
                "atlchannel",
                "bincc",
                "GetByteArray"
        );

        Geometry geometry = new WKBReader().read(result[0]);
        System.out.println(geometry);
    }
}