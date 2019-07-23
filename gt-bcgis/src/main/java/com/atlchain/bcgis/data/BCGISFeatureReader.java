package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

import java.io.IOException;
import java.util.NoSuchElementException;

public class BCGISFeatureReader implements FeatureReader {
    @Override
    public FeatureType getFeatureType() {
        return null;
    }

    @Override
    public Feature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        return null;
    }

    @Override
    public boolean hasNext() throws IOException {
        return false;
    }

    @Override
    public void close() throws IOException {

    }
}
