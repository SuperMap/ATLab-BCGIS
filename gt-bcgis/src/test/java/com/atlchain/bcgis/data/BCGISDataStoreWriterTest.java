package com.atlchain.bcgis.data;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class BCGISDataStoreWriterTest {
    private File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
    private File keyFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());
    private String peerName = "TestOrgA";
    private String peerUrl = "grpc://172.16.15.66:7051";
    private String mspId = "TestOrgA";
    private String userName= "admin";
    private String ordererName= "OrdererTestOrgA";
    private String ordererUrl = "grpc://172.16.15.66:7050";
    private String channelName = "atlchannel" ;
    private String chaincodeName = "bincc";
    private String functionName = "GetByteArray";
    private String recordKey = "LineWrite2";

    private BCGISDataStore bcgisDataStore = new BCGISDataStore(
            certFile,
            keyFile,
            peerName,
            peerUrl,
            mspId,
            userName,
            ordererName,
            ordererUrl,
            channelName,
            chaincodeName,
            functionName,
            recordKey
    );

    @Test
    public void testFeatureSource() throws IOException {
        String typeName = bcgisDataStore.getTypeNames()[0];
        SimpleFeatureType  featureType = bcgisDataStore.getSchema(typeName);
        SimpleFeatureStore featureStore = (SimpleFeatureStore) bcgisDataStore.getFeatureSource(typeName);
        SimpleFeatureStore featureStore1 = (SimpleFeatureStore) bcgisDataStore.getFeatureSource(typeName);
        SimpleFeatureStore featureStore2 = (SimpleFeatureStore) bcgisDataStore.getFeatureSource(typeName);

        Transaction t1 = new DefaultTransaction("t1");
        Transaction t2 = new DefaultTransaction("t2");

        featureStore1.setTransaction(t1);
        featureStore2.setTransaction(t2);

        System.out.println("Step 1");
        System.out.println("------");
        System.out.println(
                "start     auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println(
                "start              t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println(
                "start              t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));

        // 测试删除 featureStore1 中的数据，在事务 t1 commit 之前，删除操作只会记录在 DataStore 中，commit 之后 才会写入数据库
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
        Filter filter1 = ff.id(Collections.singleton(ff.featureId("tmpTypeName.1")));
        featureStore1.removeFeatures(filter1);
        System.out.println();
        System.out.println("Step 2 transaction 1 removes feature 'fid1'");
        System.out.println("------");
        System.out.println(
                "t1 remove auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println(
                "t1 remove          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println(
                "t1 remove          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));


        GeometryFactory geometryFactory = new GeometryFactory();
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{new Coordinate(10.0, 13.0), new Coordinate(23.0, 26.0)});
        LineString[] lineStrings = {lineString, lineString};
        MultiLineString multiLineString = geometryFactory.createMultiLineString(lineStrings);
        SimpleFeature feature = SimpleFeatureBuilder.build(featureType, new Object[]{multiLineString}, "line1");
        SimpleFeatureCollection collection = DataUtilities.collection(feature);
        featureStore2.addFeatures(collection);

        System.out.println();
        System.out.println("Step 3 transaction 2 adds a new feature '" + feature.getID() + "'");
        System.out.println("------");
        System.out.println(
                "t2 add    auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println(
                "t2 add             t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println(
                "t1 add             t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));

        // 提交事务
        t1.commit();
        System.out.println();
        System.out.println("Step 4 transaction 1 commits the removal of feature 'fid1'");
        System.out.println("------");
        System.out.println(
                "t1 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println(
                "t1 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println(
                "t1 commit          t2: " + DataUtilities.fidSet(featureStore2.getFeatures()));

//        t2.commit();
        System.out.println();
        System.out.println(
                "Step 5 transaction 2 commits the addition of '" + feature.getID() + "'");
        System.out.println("------");
        System.out.println(
                "t2 commit auto-commit: " + DataUtilities.fidSet(featureStore.getFeatures()));
        System.out.println(
                "t2 commit          t1: " + DataUtilities.fidSet(featureStore1.getFeatures()));
        System.out.println(
                "t2 commit          t2: " + DataUtilities.fidSet(((SimpleFeatureStore)bcgisDataStore.getFeatureSource(typeName)).getFeatures()));

        t1.close();
        t2.close();
        bcgisDataStore.dispose();
    }
}
