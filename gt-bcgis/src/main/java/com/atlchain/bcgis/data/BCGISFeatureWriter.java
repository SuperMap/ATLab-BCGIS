package com.atlchain.bcgis.data;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;

public class BCGISFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    public BCGISFeatureWriter(ContentState state, Query query){
//        super(entry, query);
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return null;
    }

    @Override
    public SimpleFeature next() throws IOException {
        return null;
    }

    @Override
    public void remove() throws IOException {

    }

    @Override
    public void write() throws IOException {

    }

    @Override
    public boolean hasNext() throws IOException {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}

