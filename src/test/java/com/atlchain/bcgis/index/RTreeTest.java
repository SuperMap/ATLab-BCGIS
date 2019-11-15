package com.atlchain.bcgis.index;

import com.atlchain.bcgis.data.Shp2Wkb;
import com.atlchain.bcgis.data.index.RTreeIndex;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class RTreeTest {

    String indexFilePath = "E:\\SuperMapData\\rtree.bin";
    private String shpURL = this.getClass().getResource("/D/D.shp").getFile();
    private File shpFile = new File(shpURL);
    private Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);
    List<Geometry> geometryList = shp2WKB.getGeometry();
    private RTreeIndex rTreeIndex = new RTreeIndex();
    public RTreeTest() throws IOException {
    }

    @Test
    public void createRtree() {
        STRtree stRtree = rTreeIndex.createRtree(geometryList);
        System.out.println(stRtree.size());
        System.out.println(stRtree.depth());
        assertNotEquals(0, stRtree.size());
    }

    @Test
    public void query() {
        STRtree stRtree = rTreeIndex.createRtree(geometryList);
        Geometry searchGeo = rTreeIndex.generateSearchGeo(116  , 40, 116.01, 40.01);
//        System.out.println(searchGeo);
        List<Geometry> list = rTreeIndex.query(stRtree, searchGeo);
        for (Geometry geometry: list) {
            System.out.println(geometry);
        }
    }

    @Test
    public void saveSTRtree() {
        STRtree stRtree = rTreeIndex.createRtree(geometryList);
        rTreeIndex.saveSTRtree(stRtree, indexFilePath);
        System.out.println("save the Rtree in the: " + indexFilePath);
    }

    // 载入本地 R 树进行空间查询
    @Test
    public void loadSTRtree() {
        STRtree stRtree = rTreeIndex.loadSTRtree(indexFilePath);
        int depth = stRtree.depth();
        int expected = rTreeIndex.createRtree(geometryList).depth();
        assertEquals(expected, depth);
        Geometry searchGeo = rTreeIndex.generateSearchGeo(116  , 40, 116.11, 40.11);
        List<Geometry> list = rTreeIndex.query(stRtree, searchGeo);
        for (Geometry geometry: list) {
            System.out.println(geometry);
        }
    }
}