package com.atlchain.bcgis.data;

import com.atlchain.sdk.ATLChain;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureWriter;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.util.ArrayList;

public class BCGISFeatureWriter implements SimpleFeatureWriter {

    private ContentState state;

    private Geometry geometry;

    private int index = 0;

    private SimpleFeature currentFeature;

    private SimpleFeatureBuilder builder;

    private ArrayList<Geometry> geometryArrayList = new ArrayList<>();

    public BCGISFeatureWriter(ContentState state, Geometry geometry) {
        this.state = state;
        this.geometry = geometry;
        this.builder = new SimpleFeatureBuilder(state.getFeatureType());
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return state.getFeatureType();
    }

    @Override
    public boolean hasNext() {
        if (index < geometry.getNumGeometries()){
            return true;
        } else if (geometry == null){
            return  false;
        } else {
            currentFeature = getFeature(geometry.getGeometryN(geometry.getNumGeometries() - 1));
            return false;
        }
    }

    @Override
    public SimpleFeature next() {
        if(this.currentFeature != null){
            this.write();// the previous one was not written, so do it now.
        }
        Geometry geom = null;
        if(hasNext()){
            geom = geometry.getGeometryN(index);
            currentFeature = getFeature(geom);
            index ++;
        } else {
            SimpleFeatureType featureType = state.getFeatureType();
            String fid = featureType.getTypeName() + "." + index;
            Object values[] = DataUtilities.defaultValues(featureType);
            currentFeature = SimpleFeatureBuilder.build(featureType, values, fid);
        }
        return currentFeature;
    }

    private SimpleFeature getFeature(Geometry geometry) {
        if(geometry == null){
            return null;
        }
        builder.set("geom", geometry);
        return builder.buildFeature(state.getEntry().getTypeName() + "." + index);
    }

    @Override
    public void remove() {
        currentFeature = null;
    }

    @Override
    public void write() {
        if (currentFeature == null) {
            return; 
        }
        for(Property property: currentFeature.getProperties()){
            Object value = property.getValue();
            if(value == null){
                return;
            }else if(value instanceof Geometry){
                Geometry geometry = (Geometry)value;
                geometryArrayList.add(geometry);
            }
        }
        this.currentFeature = null;
    }

    @Override
    public void close() {
        if (geometryArrayList != null) {
            this.write();
        }

        while (hasNext()) {
            next();
            write();
        }

        Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
        GeometryCollection geometryCollection = Utils.getGeometryCollection(geometries);

        File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
        File skFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());

        ATLChain atlChain = new ATLChain(
                certFile,
                skFile,
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050"
        );

        WKBWriter wkbWriter = new WKBWriter();
        byte[] bytes = wkbWriter.write(geometryCollection);
        byte[] byteKey =  "LineWrite2".getBytes();

        String result = atlChain.invokeByte(
                "atlchannel",
                "bincc",
                "PutByteArray",
                new byte[][]{byteKey, bytes}
        );
    }
}

