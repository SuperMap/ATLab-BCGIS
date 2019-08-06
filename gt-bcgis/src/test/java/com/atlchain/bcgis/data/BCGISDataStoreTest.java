package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;

public class BCGISDataStoreTest {

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
    private String recordKey = "Line";

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
    public void testRead() {
        Geometry geometry = bcgisDataStore.getRecord();
        Assert.assertNotNull(geometry);
//        System.out.println(geometry.toString());
//        String type = geometry.getGeometryType();
//        System.out.println("type ==> " + type);
//        System.out.println("count ==> " + geometry.getNumGeometries());
    }

    @Test
    public void testGetTypeNames() throws IOException {
        String[] names = bcgisDataStore.getTypeNames();
        Assert.assertNotNull(names);
//        System.out.println("typenames: " + names.length);
//        System.out.println("typename[0]: " + names[0]);
    }

    @Test
    public void testGetSchema() throws IOException {
        SimpleFeatureType type = bcgisDataStore.getSchema(bcgisDataStore.getTypeNames()[0]);
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
//                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
                    Assert.assertNotNull(feature);
                    count++;
                }
            }
//            System.out.println("close feature reader");
//            System.out.println("read in " + count + " features");
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
    public void testLoadMap() {
//        SimpleFeatureSource simpleFeatureSource = bcgisDataStore.getFeatureSource();
    }

}