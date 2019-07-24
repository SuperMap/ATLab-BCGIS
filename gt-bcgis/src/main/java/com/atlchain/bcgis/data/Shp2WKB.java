package com.atlchain.bcgis.data;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.InStream;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Shp2WKB {
    private URL shpURL = null;
    private URL wkbURL = null;

    public Shp2WKB(URL shpURL) {
        this.shpURL = shpURL;
    }


    // 将 WKB 保存到文件
    public boolean save() throws IOException {


        // TODO 将 geometry 保存到文件中
        //  1、GeometryCollection geometries转化为Geometry  因为是父类，直接赋值给他，可自动转型
        //  2、通过WKB的wirte方法将geometries写入到字节数组 byte[] 中为 WKBfile
        //  3、通过I/O的方式创建新文件 f (WKB.wkb) 写入 字节数组 byte[]


        //  Geometry是GeometryCollection 的父类 直接等于它就直接转型了
        //  GeometryCollection geometries = readShpFile();// 原来的
        Geometry geometries = readShpFile();
        // 	write(Geometry geom) 将Geometry存到字节数组。
        System.out.println("原文件"+geometries);   //===========================这是前面的Geometry格式，然后后面读取进来之后再对比
        /*
        I/O输出流  写入信息   ============创建新文件对象 f
         */
        WKBWriter writer = new WKBWriter();
        byte[] WKBfileout = writer.write(geometries);
        String url = this.getClass().getResource("/WKB.wkb").getPath();
        File f = new File(url);
        FileOutputStream out = null;// 先给定一个空值
        out = new FileOutputStream(f);//实例化（并添加异常）
        // 假如是字符串str 字符串转化为字节数组   byte b[] = str.getBytes()
        out.write(WKBfileout); // 将字节数组写入到文件中
        out.close(); // 关闭  需添加异常
        /*
        读取   WKB.wkb  里面的信息
         */

        String ur2 = this.getClass().getResource("/WKB.wkb").getPath();
        File f2 = new File(ur2);
        byte[] fileBytes = Files.readAllBytes(Paths.get(f2.getPath()));
          // fileBytes  存为二进制
        WKBReader reader = new WKBReader();
        Geometry geometry = null;
        try {
           geometry = reader.read(fileBytes);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(geometry);


        return false;
    }

    // 返回 Geometry 对象
    public GeometryCollection getGeometry() throws IOException {
        return readShpFile();
    }

    // TODO   读取并解析 ShapeFile 文件
    private GeometryCollection readShpFile() throws IOException{
        // 读取文件数据集   store就是获取了这个数据
        FileDataStore store = FileDataStoreFinder.getDataStore(shpURL);
        // 从数据集中获取属性源
        SimpleFeatureSource featureSource = store.getFeatureSource();
        // 获取属性集合  即是全部的属性   可以一行行的表示出来
        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
        // 获取属性集合的迭代器   弄成迭代器的形式方便后面把里面的数据打印出来
        SimpleFeatureIterator featureIterator = featureCollection.features();


        // TODO 构造该集合对象   建立有序的数组ArrayList方便存放
        ArrayList<Geometry> geometryArrayList = new ArrayList<>();

        // TODO 将集合对象保存到 geometryCollection 中并返回
        // 遍历属性集合
        while (featureIterator.hasNext()) {
            SimpleFeature feature = featureIterator.next();//每次获取里面的一行值
            // 获取属性中的空间集合对象
            Object geomObj = feature.getDefaultGeometry();
            //  然后将得到的一组组属性geomObj 放入到建立的有序的数组里面 geometryArrayList  这里面也进行了转型
            geometryArrayList.add((Geometry) geomObj);
            //  下面代码后期需去掉  这里这样可将得到的每一行数据打印出来  上面就是直接添加到新的数组里面
            //System.out.println(geomObj.toString());
        }

        //   将得到的数组geometryArrayList 向上转型为Geometry的数组 geometries
        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);

        //   保存空间几何对象  ========为什么原来那个数组不行呢？====因为下面规定了需要Geometry 对象  所以上面要转型
        GeometryCollection geometryCollection = getGeometryCollection(geometries);
        return geometryCollection;

    }

    // 获取 GeometryCollection 对象
    private GeometryCollection getGeometryCollection(Geometry[] geomList) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return new GeometryCollection(geomList, geometryFactory);
    }


    //    // TODO
//    // 读取并解析 ShapeFile 文件
//    private GeometryCollection readShpFile() throws IOException{
//        // 读取文件数据集
//        FileDataStore store = FileDataStoreFinder.getDataStore(shpURL);
//        // 从数据集中获取属性源
//        SimpleFeatureSource featureSource = store.getFeatureSource();
//        // 获取属性集合
//        SimpleFeatureCollection featureCollection = featureSource.getFeatures();
//        // 获取属性集合的迭代器
//        SimpleFeatureIterator featureIterator = featureCollection.features();
//        // 现在shp文件里面的数据都在featureIterator里面  现在的目的是将其保存为空间几何数据
//        // TODO 构造该集合对象集合
//        Geometry[] geometries = null;
//        // TODO 将集合对象保存到 geometryCollection 中并返回
//        // 遍历属性集合
//        while (featureIterator.hasNext()) {
//            SimpleFeature feature = featureIterator.next();
//            // 获取属性中的空间集合对象
//            Object geomObj = feature.getDefaultGeometry();
//        }
//        GeometryCollection geometryCollection = getGeometryCollection(geometries);
//        return geometryCollection;
//    }

}
