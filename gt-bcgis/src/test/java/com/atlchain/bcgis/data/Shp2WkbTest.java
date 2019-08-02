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
    String shpURL = this.getClass().getResource("/Line/Line.shp").getFile();
    File shpFile = new File(shpURL);
    Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);

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
            Assert.assertEquals(111, geometryCollection.getNumGeometries());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveWKB(){
        try {
            String path = "/tmp/test.wkb";
            shp2WKB.save(new File(path));
            Assert.assertTrue(Files.exists(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveGeometryToChain() throws IOException {
        File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
        File skFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());

        ATLChain atlChain = new ATLChain(
                certFile,
                skFile,
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050"
        );
        GeometryCollection geometryCollection = shp2WKB.getGeometry();

        WKBWriter wkbWriter = new WKBWriter();
        byte[] bytes = wkbWriter.write(geometryCollection);
        byte[] byteKey =  "Line".getBytes();

        String result = atlChain.invokeByte(
                "atlchannel",
                "bincc",
                "PutByteArray",
                new byte[][]{byteKey, bytes}
        );
        System.out.println(result);
    }

    @Test
    public void testQueryGeometryFromChain() throws IOException, ParseException {
        File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
        File skFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());

        ATLChain atlChain = new ATLChain(
                certFile,
                skFile,
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050"
        );
        GeometryCollection geometryCollection = shp2WKB.getGeometry();

        WKBWriter wkbWriter = new WKBWriter();
        byte[] bytes = wkbWriter.write(geometryCollection);
        byte[] byteKey =  "Line".getBytes();

        byte[][] result = atlChain.queryByte(
                "atlchannel",
                "bincc",
                "GetByteArray",
                new byte[][]{byteKey}
        );

        Geometry geometry = new WKBReader().read(result[0]);
        System.out.println(geometry);
    }
}