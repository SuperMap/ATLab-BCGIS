package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.NoSuchElementException;

public class BCGISFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {
    protected ContentState state;

    protected Geometry geometry;

    protected SimpleFeatureBuilder builder;

    protected GeometryFactory geometryFactory;

    private int index = 0;

    private GeometryCollectionIterator iterator;

    public BCGISFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        BCGISDataStore bcgisDataStore = (BCGISDataStore) contentState.getEntry().getDataStore();
        geometry = bcgisDataStore.read();
        iterator = new GeometryCollectionIterator(geometry);
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) state.getFeatureType();
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        Geometry _geom = (Geometry) iterator.next();
        if (! iterator.hasNext()) {
            close();
            return null;
        }
        Geometry geom = (Geometry) iterator.next();
        return getFeature(geom);
    }

    private SimpleFeature getFeature(Geometry geometry) {
        index ++;
        builder.set("geom", geometry);
        return builder.buildFeature(state.getEntry().getTypeName() + "." + index);
    }

    @Override
    public boolean hasNext() throws IOException {
        return iterator.hasNext();
    }

    @Override
    public void close() throws IOException {
        builder = null;
        geometryFactory = null;
    }
}
