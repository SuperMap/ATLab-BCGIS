package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
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

    public BCGISFeatureReader(ContentState contentState) {
        this.state = contentState;
        BCGISDataStore bcgisDataStore = (BCGISDataStore) contentState.getEntry().getDataStore();
        geometry = bcgisDataStore.getRecord();
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return state.getFeatureType();
    }

    private SimpleFeature next;

    @Override
    public SimpleFeature next() throws IllegalArgumentException, NoSuchElementException {
        SimpleFeature feature;
        if(next != null){
            feature = next;
            next = null;
        }else{
            Geometry geom = geometry.getGeometryN(index);
            feature = getFeature(geom);
        }
        return feature;
    }

    private SimpleFeature getFeature(Geometry geometry) {
        if(geometry == null){
            return null;
        }
        index ++;
        builder.set("geom", geometry);
        return builder.buildFeature(state.getEntry().getTypeName() + "." + index);
    }

    @Override
    public boolean hasNext() throws IOException {
        if (index < geometry.getNumGeometries()){
            return true;
        }else if(geometry == null){
            return  false;
        } else{
            next = getFeature(geometry);
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        builder = null;
        geometryFactory = null;
        next = null;
    }
}
