package com.atlchain.bcgis.data;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.GeometryCollection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class Shp2WKBTest {
    URL shpURL = this.getClass().getResource("/BL/BL.shp");
    Shp2WKB shp2WKB = new Shp2WKB(shpURL);

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
            boolean bool = shp2WKB.save();
//            Assert.assertEquals(140, geometryCollection.getNumGeometries());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}