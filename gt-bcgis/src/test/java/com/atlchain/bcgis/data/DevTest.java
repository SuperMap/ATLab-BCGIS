//package com.atlchain.bcgis.data;
//
//import org.geotools.data.*;
//
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.data.simple.SimpleFeatureIterator;
//import org.geotools.data.simple.SimpleFeatureSource;
//import org.geotools.data.simple.SimpleFeatureStore;
//import org.geotools.factory.CommonFactoryFinder;
//import org.geotools.feature.DefaultFeatureCollection;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.geotools.geometry.jts.MultiCurve;
//import org.junit.Test;
//import org.locationtech.jts.geom.*;
//import org.locationtech.jts.io.ParseException;
//import org.locationtech.jts.io.WKBReader;
//import org.opengis.feature.Property;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.feature.type.AttributeDescriptor;
//import org.opengis.filter.Filter;
//import org.opengis.filter.FilterFactory;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.Serializable;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.List;
//
//public class DevTest {
//
//    File file = new File("E:\\DemoRecording\\WkbCode\\Line.wkb");
////    BCGISDataStore WKB = new BCGISDataStore();
//
//    // 读取wkb文件为geometry并展示
//    @Test
//    public void testReadWkb(){
//        File f2 = new File("/home/cy/Documents/ATL/SuperMap/ATLab-BCGIS/gt-bcgis/src/main/resources/WKB.wkb");
//        byte[] fileBytes = new byte[0];
//        try {
//            fileBytes = Files.readAllBytes(Paths.get(f2.getPath()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        WKBReader reader = new WKBReader();
//        Geometry geometry = null;
//        try {
//            geometry = reader.read(fileBytes);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        for(int i = 0; i <geometry.getNumGeometries();i++)
//            System.out.println(geometry.getGeometryN(i));
//        System.out.println(geometry.getNumGeometries());
//    }
//
//
//    // 测试获取wkb的读取功能并打印出空间几何要素
//    @Test
//    public void testRead() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
//
////        Geometry geometry = WKB.getRecord();
//        Geometry geometry = null;
//        for(int i = 0; i < geometry.getNumGeometries();i++)
//            System.out.println(geometry.getGeometryN(i));
//
//    }
//
//    // 根据文档开始测试
//
//    //测试获取wkb文件名
//    @Test
//    public void getTypeNames() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore store = DataStoreFinder.getDataStore(params);
//
//        String names[] = store.getTypeNames();
//        System.out.println("typenames: " + names.length);
//        System.out.println("typename[0]: " + names[0]);
//    }
//
//    // Test DataStore.getSchema( typeName ) The method provides access to a FeatureType referenced by a type name
//    @Test
//    public void getSchema() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore store = DataStoreFinder.getDataStore(params);
//
//        SimpleFeatureType type = store.getSchema(store.getTypeNames()[0]);                              // 为什么是0 ，可能是因为这里只存储了一个  后面应该多了就有了
//
//        System.out.println("featureType  name: " + type.getName());
//        System.out.println("featureType count: " + type.getAttributeCount());                           //返回有几个特征值
//
//        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
//            System.out.print("  " + descriptor.getName());
//            System.out.print(" (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
//            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
//            System.out.print(" type: " + descriptor.getType().getName());
//            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
//        }
//
//        // access by index 因为现在主要只定义了 FeatureID
//        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
//        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
//        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
//        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());
//
//    }
//
//    // Test DataStore.getFeatureReader( query, transaction )  The method allows access to the contents of our DataStore
//    // 空间几何对象特征获取测试
//    @Test
//    public void getFeatureReader() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//
//        Query query = new Query(datastore.getTypeNames()[0]);
//
//        System.out.println("open feature reader");
//        FeatureReader<SimpleFeatureType, SimpleFeature> reader =
//                datastore.getFeatureReader(query, Transaction.AUTO_COMMIT);
//        try {
//            int count = 0;
//            while (reader.hasNext()) {
//                SimpleFeature feature = reader.next();
//                if (feature != null) {
//                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
//                    count++;
//                }
//            }
//            System.out.println("close feature reader");
//            System.out.println("read in " + count + " features");
//        } finally {
//            reader.close();
//        }
//    }
//
//    // 可以进行查询
//    // Example with a quick “selection” Filter:
//    @Test
//    public void Selection() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        Query query = new Query(datastore.getTypeNames()[0]);
//
//        FeatureReader<SimpleFeatureType,SimpleFeature> reader =
//                datastore.getFeatureReader(query,Transaction.AUTO_COMMIT);
//
//        try {
//            while (reader.hasNext()){
//                SimpleFeature feature = reader.next();
//                if(reader.hasNext() == false) break;                                                // 增加if语句 跳出循环 防止空指针异常发生
////                System.out.println(feature);
//                System.out.println(reader.hasNext());
//                for (Property property : feature.getProperties()){
//                    System.out.println("\t");
//                    System.out.println( property.getName());
//                    System.out.println("=");
//                    System.out.println(property.getValue());
//                }
//            }
//        } finally {
//            reader.close();
//        }
//    }
////
//    // Test DataStore.getFeatureSource( typeName )
//    // 原测试里面的 CQL 查询测试 （未做） 原文是按照城市名，即属性查询，我这里没有怎么办，暂时不查询
//    @Test
//    public void getFeatureSource() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
//    }
//
//    @Test
//    public void FeatureCollection() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
//        SimpleFeatureCollection featureCollection = featureSource.getFeatures();                // 获取所有属性到featureCollection里面
//
//        List<String>list = new ArrayList<>();
//        //FeatureCollection.features() - access to a FeatureIterator
//        SimpleFeatureIterator features = featureCollection.features();
//        while(features.hasNext()){
////                if(features.hasNext() == false) break;
////                System.out.println(features.hasNext());
//            if (list.size() == 5) break;
//            list.add(features.next().getID());
//        }
//
//        System.out.println("List Contents:" +  list);
//        System.out.println("FeatureSource count :       " + featureSource.getCount(Query.ALL));     //返回多少个特征值
//        System.out.println("FeatureSource bounds:       " + featureSource.getBounds(Query.ALL));    //返回边界查询
//        System.out.println("FeatureCollection bounds:   " + featureCollection.size());;
//        System.out.println("FeatureCollection bounds:   " + featureCollection.getBounds());
//
//        // Load the feature into memory
//        DefaultFeatureCollection collection = DataUtilities.collection(featureCollection);
//        System.out.println("       Collection size:"  +collection.size());
//    }
//
//
//    // =====================2019.7.30 ===========前测试已完成（问题是两个hashNext有点问题需要解决）
//    // ====================后面测试主要是涉及到加入 BCGISFeatureStore 和 BCGISFeatureWriter 之后的测试
//
//
//    // FeatureStore provides Transaction support and modification operations. FeatureStore is an extension of FeatureSource
//    // check the result of getFeatureSource( typeName ) with the instanceof operator
//    @Test
//    public void FeatureStoreDemo() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        SimpleFeatureSource featureSource = datastore.getFeatureSource("Line");
//
//        if(!(featureSource instanceof SimpleFeatureStore)){
//            try {
//                throw new IllegalAccessException("Modification not supported");
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;
//        System.out.println(featureStore);
//    }
//
//
//    // FeatureStore 使用实例
//    @Test
//    public void FeatureStore() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        // DefaultTransaction(String handle)  Quick implementation of Transaction api.
//        Transaction t1 = new DefaultTransaction("transactions 1");
//        Transaction t2 = new DefaultTransaction("transactions 2");
//
//        SimpleFeatureType type = datastore.getSchema("Line");
//
//        //  DataStore.getFeatureSource( typeName ) method is the gateway to our high level api, as provided by an instance of FeatureSource, FeatureStore or FeatureLocking
//        //  getFeatureSource( typeName )由FeatureSource、FeatureStore或FeatureLocking实例提供
//        SimpleFeatureStore featureStore  = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
//        SimpleFeatureStore featureStore1 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
//        SimpleFeatureStore featureStore2 = (SimpleFeatureStore)datastore.getFeatureSource("Line") ;
//        //  FeatureStore.setTransaction(Transaction transaction)  Provide a transaction for commit/rollback control of a modifying operation on this FeatureStore
//        featureStore1.setTransaction(t1);
//        featureStore2.setTransaction(t2);
//
////        System.out.println("Step 1");
////        System.out.println("------");
////        // DataUtilities.fidSet(FeatureCollection<?,?> featureCollection)
////        // Copies the feature ids from each and every feature into a set
////        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));                 // auto-commit”表示磁盘上文件的当前内容
////        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore1.getFeatures()));
////        System.out.println("start    auto-commit:" + DataUtilities.fidSet(featureStore2.getFeatures()));
//
//
////        // select feature to remove
////        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
////        Filter filter1 = ff.id(Collections.singleton(ff.featureId("Line.1")));
////        featureStore.removeFeatures(filter1);
////        System.out.println();
////        System.out.println("Step 2 transaction 1 removes feature 'fid1'");
////        System.out.println("-------");
////        System.out.println("t1 remove auto-commit:" + DataUtilities.fidSet(featureStore.getFeatures()));
////        System.out.println("t1 remove          t1:" + DataUtilities.fidSet(featureStore1.getFeatures()));
////        System.out.println("t1 remove          t2:" + DataUtilities.fidSet(featureStore2.getFeatures()));
//
//
//        // new feature to add!
//        SimpleFeatureCollection  featureCollection = datastore.getFeatureSource(datastore.getTypeNames()[0]).getFeatures();
//        SimpleFeature simpleFeature = featureCollection.features().next();
//        List<Object> obj = simpleFeature.getAttributes();
//        MultiLineString multiLineString = (MultiLineString) obj.get(0);
//
//        SimpleFeature feature = SimpleFeatureBuilder.build(type,new Object[]{ multiLineString },"Line.1");
//        SimpleFeatureCollection collection = DataUtilities.collection(feature);
//        System.out.println(">>>>>>multiLineString<<<<<" + multiLineString);
//        System.out.println("<<<<<<collection     <<<<<" + collection);
//
//        featureStore2.addFeatures(collection);
//
//        SimpleFeatureCollection featureCollection1 = featureStore2.getFeatures();
//        SimpleFeatureIterator iterator = featureCollection1.features();
//        while (iterator.hasNext()) {
//            System.out.println("==+++======"+iterator.next().toString());
//        }
////        GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
////        Point bb = gf.createPoint(new Coordinate(75,444));
////        SimpleFeature feature1 = SimpleFeatureBuilder.build(type,new Object[]{bb},"Line11");
////        SimpleFeatureCollection collection1 = DataUtilities.collection(feature1);
////        System.out.println("========collection1=======" + collection1);
////        featureStore2.addFeatures(collection1);
//
//        //
//        System.out.println();
//        System.out.println("Step  3 transaction 2 adds a new feature " + feature.getID()+"'");
//        System.out.println("---------");
//        System.out.println("t2 add auto-commit:"+DataUtilities.fidSet(featureStore.getFeatures()));
//        System.out.println("t2 add          t1:"+DataUtilities.fidSet(featureStore1.getFeatures()));
//        System.out.println("t1 add          t2:"+DataUtilities.fidSet(featureStore2.getFeatures()));// 这一步对featureStore2 不产生变化
//        System.out.println();
//
//
//
//        //提交事务1
////        t1.commit();
////
////        System.out.println();
////        System.out.println("Step 4 transaction 1 commits the removal of feature 'fid1'");
////        System.out.println("------");
////        System.out.println("t1 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
////        System.out.println("t1 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
////        System.out.println("t1 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));
////        System.out.println(featureStore2);
//
//        //提交事务2
//        System.out.println("qian");
//        t2.commit();
//        System.out.println("hou");
////        SimpleFeatureCollection featureCollection3 = featureStore2.getFeatures();
////        SimpleFeatureIterator iterator3 = featureCollection3.features();
////        while (iterator3.hasNext()) {
////            System.out.println("==+++======"+iterator3.next().toString());
////        }
//
//        System.out.println();
//        System.out.println("Step 5 transaction 2 commits the addition of '" + feature.getID() + "'");
//        System.out.println("------");
//        System.out.println("t2 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
//        System.out.println("t2 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
//        System.out.println("t2 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));
//
//        // Frees all State held by this Transaction.
////        t1.close();
//        t2.close();
//        datastore.dispose();
//    }
//
//    @Test
//    public void testFeatureWriter() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//        Transaction t = new DefaultTransaction("Line");
////        Transaction t2 = new DefaultTransaction("transactions 2");
//        try{
//            FeatureWriter<SimpleFeatureType,SimpleFeature>writer = datastore.getFeatureWriter("Line",Filter.INCLUDE,t);
//            SimpleFeature feature ;
//            try{
//                while(writer.hasNext()){
//                    feature = writer.next();
//                    System.out.println("remove " + feature.getID());
//                    writer.remove();// Removes current Feature, must be called before hasNext.
//                }
//            }finally {
//                writer.close();
//            }
//            t.commit();
//        }catch(Throwable eek){
//            t.rollback();
//        }finally {
//            t.close();
//            datastore.dispose();
//        }
//        System.out.println("commit " + t); //输出 commit Line  即 t = Line
//    }
//
//
//    //  completely replace all features  思路是先删除，然后增加feature
//    @Test
//    public void TestFeatureWriter() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//
//        final SimpleFeatureType type = datastore.getSchema("Line");
//        final FeatureWriter<SimpleFeatureType,SimpleFeature>writer;
//        SimpleFeature f;
//        DefaultFeatureCollection collection = new DefaultFeatureCollection();
//
//        // new add
//        SimpleFeatureCollection  featureCollection = datastore.getFeatureSource(datastore.getTypeNames()[0]).getFeatures();
//        SimpleFeature simpleFeature = featureCollection.features().next();
//        List<Object> obj = simpleFeature.getAttributes();
//        MultiLineString multiLineString = (MultiLineString) obj.get(0);
////        System.out.println(">>>>>>>" + multiLineString);
//        SimpleFeature bf = SimpleFeatureBuilder.build(type,new Object[]{ multiLineString },"Line.9");
//
//        collection.add(bf);
//
//        writer = datastore.getFeatureWriter("Line",Transaction.AUTO_COMMIT);
//        try{
////            // remove all features
////            while(writer.hasNext()){
////                writer.next();
////                writer.remove();
////            }
//            // copy new features in
//            SimpleFeatureIterator iterator = collection.features();
//            while(iterator.hasNext()){
//                SimpleFeature feature = iterator.next();
//                SimpleFeature newFeature = writer.next();//new blank feature
//                newFeature.setAttributes(feature.getAttributes());
//                writer.write();
//                // 输出结果多了一个新的元素在writer里面
//                System.out.println(writer.getFeatureType());
//            }
//        }finally{
//            writer.close();
//        }
//    }
//
//    // ===============暂时不写，目前DataStore里面的 createSchema 尚未实现
//    // making a copy
//    @Test
//    public  void TestgetFeatureWriterAppend() throws IOException {
//        Map<String, Serializable> params = new HashMap<>();
//        params.put("file", file);
//        DataStore datastore = DataStoreFinder.getDataStore(params);
//
//        final SimpleFeatureType type = datastore.getSchema("Line");
//
//    }
//
//}