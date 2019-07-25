package com.atlchain.bcgis.data;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.type.Name;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class BCGISDataStore extends ContentDataStore {
    private File file;

    public BCGISDataStore(File file) {
        this.file = file;
    }

    Geometry read() throws IOException {
        WKBReader reader = new WKBReader();
        Geometry geometry = null;
        try {
            geometry = reader.read(Files.readAllBytes(Paths.get(file.getPath())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return geometry;
    }

    @Override
    protected List<Name> createTypeNames() throws IOException {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));

        Name typeName = new NameImpl(name);
        return Collections.singletonList(typeName);
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new BCGISFeatureSource(entry, Query.ALL);
    }
}