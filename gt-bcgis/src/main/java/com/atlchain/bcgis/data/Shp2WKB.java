package com.atlchain.bcgis.data;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.net.URL;

public class Shp2WKB {
    private URL shpURL = null;
    private URL wkbURL = null;

    public Shp2WKB(URL shpURL) {
        this.shpURL = shpURL;
    }


    // 将 WKB 保存到文件
    public boolean save() throws IOException {
        GeometryCollection geometries = readShpFile();
        // TODO 将 geometry 保存到文件中

        return false;
    }

    // 返回 Geometry 对象
    public GeometryCollection getGeometry() throws IOException {
        return readShpFile();
    }

    // TODO
    // 读取并解析 ShapeFile 文件
    private GeometryCollection readShpFile() throws IOException{
        // 读取文件数据集
        FileDataStore store = FileDataStoreFinder.getDataStore(shpURL);
        // 从数据集中获取属性源
        SimpleFeatureSource featureSource = store.getFeatureSource();
        // 获取属性集合
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        // 获取属性集合的迭代器
        SimpleFeatureIterator featureIterator = featureCollection.features();

        // TODO 构造该集合对象集合
        Geometry[] geometries = null;

        // TODO 将集合对象保存到 geometryCollection 中并返回
        // 遍历属性集合
        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();
            // 获取属性中的空间集合对象
            Object geomObj = feature.getDefaultGeometry();

        }
        GeometryCollection geometryCollection = getGeometryCollection(geometries);

        return geometryCollection;
    }

    // 获取 GeometryCollection 对象
    private GeometryCollection getGeometryCollection(Geometry[] geomList) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return new GeometryCollection(geomList, geometryFactory);
    }
}
