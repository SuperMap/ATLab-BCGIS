package com.atlchain.bcgis.data;

import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BCGISDataStoreReaderTest {

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
        ContentFeatureSource featureSource = bcgisDataStore.getFeatureSource(bcgisDataStore.getTypeNames()[0]);
        System.out.println(featureSource.getSchema());
    }

    @Test
    public void testGetTypeNames() throws IOException {
        String[] names = bcgisDataStore.getTypeNames();
        Assert.assertNotNull(names);
//        System.out.println("typenames: " + names.length);
//        System.out.println("typename[0]: " + names[0]);
    }

    @Test
    public void testGetGeometryDescriptor() throws IOException {
        SimpleFeatureType type = bcgisDataStore.getSchema(bcgisDataStore.getTypeNames()[0]);
        GeometryDescriptor descriptor = type.getGeometryDescriptor();
        System.out.println(descriptor.getType());
    }

    @Test
    public void testGetBounds() throws IOException {
        ContentFeatureSource bcgisFeatureSource = bcgisDataStore.getFeatureSource(bcgisDataStore.getTypeNames()[0]);
        FeatureCollection featureCollection = bcgisFeatureSource.getFeatures();
        ReferencedEnvelope env = DataUtilities.bounds(featureCollection);
        System.out.println(env);
    }

    @Test
    public void testGetSchema() throws IOException {
        SimpleFeatureType type = bcgisDataStore.getSchema(bcgisDataStore.getTypeNames()[0]);
        System.out.println(type);
        Assert.assertNotNull(type.getAttributeDescriptors());

//        System.out.println("featureType  name: " + type.getName());
//        System.out.println("featureType count: " + type.getAttributeCount());
//
//        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
//            System.out.print("  " + descriptor.getName());
//            System.out.print(
//                    " (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
//            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
//            System.out.print(" type: " + descriptor.getType().getName());
//            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
//        }
//
//        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
//        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
//        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
//        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());
    }

    @Test
    public void testGetFeatureReaderFromFeatureSource() throws IOException {
        Query query = new Query(bcgisDataStore.getTypeNames()[0]);

        System.out.println("open feature reader");
        FeatureReader<SimpleFeatureType, SimpleFeature> reader =
                bcgisDataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            int count = 0;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                if (feature != null) {
                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
                    Assert.assertNotNull(feature);
                    count++;
                }
            }
//            System.out.println("close feature reader");
            System.out.println("read in " + count + " features");
        } finally {
            reader.close();
        }
    }

    @Test
    public void testGetFeatureCount() throws IOException {
        ContentFeatureSource bcgisFeatureSource = bcgisDataStore.getFeatureSource(bcgisDataStore.getTypeNames()[0]);
        int n = bcgisFeatureSource.getCount(Query.ALL);
        Assert.assertNotEquals(-1, n);
    }

    @Test
    public void testGetDataStoreByParam() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("bcgis", "bcgis");
        DataStore store = DataStoreFinder.getDataStore(params);
        ContentFeatureSource bcgisFeatureSource = (ContentFeatureSource) store.getFeatureSource(bcgisDataStore.getTypeNames()[0]);
        int n = bcgisFeatureSource.getCount(Query.ALL);

        String names[] = store.getTypeNames();
        System.out.println("typenames: " + names.length);
        System.out.println("typename[0]: " + names[0]);
        System.out.println(n);
    }

    // 以JFrame方式显示地图
    public static void main(String[] args) throws IOException {
        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                new File(BCGISDataStoreReaderTest.class.getResource("/certs/user/cert.pem").getPath()),
                new File(BCGISDataStoreReaderTest.class.getResource("/certs/user/user_sk").getPath()),
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050",
                "atlchannel" ,
                "bincc",
                "GetByteArray",
                "Line"
        );

       SimpleFeatureSource simpleFeatureSource = bcgisDataStore.getFeatureSource(bcgisDataStore.getTypeNames()[0]);
       simpleFeatureSource.getSchema();
       String typeName = bcgisDataStore.getTypeNames()[0];
       SimpleFeatureType type = bcgisDataStore.getSchema(typeName);
       MapContent map = new MapContent();
       map.setTitle("testBCGIS");
//        Style style = SLD.createLineStyle(Color.BLACK, 2.0f);
       Style style = SLD.createSimpleStyle(type);

       Layer layer = new FeatureLayer(simpleFeatureSource, style);
       map.addLayer(layer);

       JMapFrame.showMap(map);
   }
}