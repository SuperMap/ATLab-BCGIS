package com.atlchain.bcgis.data;

import javafx.collections.MapChangeListener;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollectionIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BCGISDataStoreTest {
    private File file = new File(this.getClass().getResource("/geometryCollection.wkb").getPath());

    @Test
    public void testRead() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Geometry geometry = new BCGISDataStore(file).read();
        System.out.println(geometry.toString());
        String type = geometry.getGeometryType();
//        System.out.println(type);
        GeometryCollectionIterator geometryCollectionIterator = new GeometryCollectionIterator(geometry);
        geometryCollectionIterator.next();
        while (geometryCollectionIterator.hasNext()) {
            Geometry geom = (Geometry) geometryCollectionIterator.next();
            System.out.println(geom.toString());
            geometryCollectionIterator.next();
        }
    }

    @Test
    public void getTypeNames() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);

        String names[] = store.getTypeNames();
        System.out.println("typenames: " + names.length);
        System.out.println("typename[0]: " + names[0]);
    }

    @Test
    public void getSchema() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore store = DataStoreFinder.getDataStore(params);

        SimpleFeatureType type = store.getSchema(store.getTypeNames()[0]);

        System.out.println("featureType  name: " + type.getName());
        System.out.println("featureType count: " + type.getAttributeCount());

        for (AttributeDescriptor descriptor : type.getAttributeDescriptors()) {
            System.out.print("  " + descriptor.getName());
            System.out.print(
                    " (" + descriptor.getMinOccurs() + "," + descriptor.getMaxOccurs() + ",");
            System.out.print((descriptor.isNillable() ? "nillable" : "manditory") + ")");
            System.out.print(" type: " + descriptor.getType().getName());
            System.out.println(" binding: " + descriptor.getType().getBinding().getSimpleName());
        }

        AttributeDescriptor attributeDescriptor = type.getDescriptor(0);
        System.out.println("attribute 0    name: " + attributeDescriptor.getName());
        System.out.println("attribute 0    type: " + attributeDescriptor.getType().toString());
        System.out.println("attribute 0 binding: " + attributeDescriptor.getType().getBinding());
    }

    @Test
    public void getFeatureReader() throws IOException {
        Map<String, Serializable> params = new HashMap<>();
        params.put("file", file);
        DataStore datastore = DataStoreFinder.getDataStore(params);

        Query query = new Query(datastore.getTypeNames()[0]);

        System.out.println("open feature reader");
        FeatureReader<SimpleFeatureType, SimpleFeature> reader =
                datastore.getFeatureReader(query, Transaction.AUTO_COMMIT);
        try {
            int count = 0;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                if (feature != null) {
                    System.out.println("  " + feature.getID() + " " + feature.getAttribute("geom"));
                    count++;
                }
            }
            System.out.println("close feature reader");
            System.out.println("read in " + count + " features");
        } finally {
            reader.close();
        }
    }
}