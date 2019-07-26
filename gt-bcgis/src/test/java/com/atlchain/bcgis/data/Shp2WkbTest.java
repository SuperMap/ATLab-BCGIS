package com.atlchain.bcgis.data;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shp2WkbTest {
    String shpURL = this.getClass().getResource("/Point/Point.shp").getFile();
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
            Assert.assertEquals(140, geometryCollection.getNumGeometries());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBoolean(){
        try {
            String path = "/tmp/test.wkb";
            shp2WKB.save(new File(path));
            Assert.assertTrue(Files.exists(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}