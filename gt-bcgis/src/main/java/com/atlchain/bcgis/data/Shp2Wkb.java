package com.atlchain.bcgis.data;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Shp2Wkb {
    private File shpFile = null;

    /**
     * Shp2Wkb
     * @param shpFile Shapefile文件
     */
    public Shp2Wkb(File shpFile) {
        this.shpFile = shpFile;
    }

    /**
     * 将Shapefile中的空间几何对象保存到WKB文件
     * @param wkbFile WKB文件
     * @throws IOException
     */
    public void save(File wkbFile) throws IOException {
        Geometry geometries = readShpFile();
        System.out.println("原文件: " + geometries.toString());

        if (!wkbFile.exists()) {
            wkbFile.createNewFile();
        }

        WKBWriter writer = new WKBWriter();
        byte[] WKBByteArray = writer.write(geometries);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(wkbFile);
            out.write(WKBByteArray);
        } finally {
            out.close();
        }
    }

    /**
     * 获取Shapefile文件中的所有空间几何对象
     * @return 包含所有空间几何对象的GeometryCollection
     * @throws IOException
     */
    public GeometryCollection getGeometry() throws IOException {
        return readShpFile();
    }

    /**
     * 读取Shapefile，将其中所有的空间几何对象保存在GeometryCollection中
     * @return 包含Shapefile中所有空间几何对象的GeometryCollection
     * @throws IOException
     */
    private GeometryCollection readShpFile() throws IOException {
        // 读取文件数据集
        FileDataStore store = FileDataStoreFinder.getDataStore(shpFile);
        // 从数据集中获取属性源
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // 获取属性
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        SimpleFeatureIterator featureIterator = featureCollection.features();
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();
        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            Object geomObj = feature.getDefaultGeometry();
            System.out.println(geomObj.toString());
            geometryArrayList.add((Geometry) geomObj);
        }

        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        GeometryCollection geometryCollection = getGeometryCollection(geometries);
        System.out.println(geometryCollection.toString());
        return geometryCollection;
    }

    /**
     * 构造GeometryCollection对象
     * @param geomList 空间几何对象列表
     * @return
     */
    private GeometryCollection getGeometryCollection(Geometry[] geomList) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return new GeometryCollection(geomList, geometryFactory);
    }

}
