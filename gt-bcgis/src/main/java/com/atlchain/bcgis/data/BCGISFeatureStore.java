package com.atlchain.bcgis.data;

import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;

public class BCGISFeatureStore extends ContentFeatureStore {
    private Geometry geometry;

    public BCGISFeatureStore(ContentEntry entry, Query query, Geometry geometry) {
        super(entry, query);
        this.geometry = geometry;
    }

    @Override
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(
            Query query, int flags) {
        return new BCGISFeatureWriter(getState(), query);
    }
}
