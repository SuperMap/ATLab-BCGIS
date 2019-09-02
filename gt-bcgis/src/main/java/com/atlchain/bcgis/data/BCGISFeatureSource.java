package com.atlchain.bcgis.data;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.util.logging.Logger;

public class BCGISFeatureSource extends ContentFeatureSource {

    Logger logger = Logger.getLogger(BCGISFeatureSource.class.toString());

    public BCGISFeatureSource(ContentEntry entry, Query query) {
        super(entry, Query.ALL);
        logger.info("=======the BCGISFeatureSource contruct=========" + entry.getTypeName());
    }

    public BCGISDataStore getDataStore() {
        logger.info("========== BCGISDataStore getDataStore() in the BCGISFeatureSource============");

        return (BCGISDataStore) super.getDataStore();
    }

    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {

        logger.info("=====================> getBoundsInternal");

        // the method getFeatures() in the FeatureReader
        FeatureCollection featureCollection = getFeatures();
        FeatureIterator iterator = featureCollection.features();
        ReferencedEnvelope env = DataUtilities.bounds(iterator);

        System.out.println("========getBoundsInternal=========>" + env.toString());

        return env;

    }

    /**
     * 根据查询条件查询属性条数
     * @param query 查询条件
     * @return 符合条件的属性条数，-1则表示不能计算该条件的数量，需要外部用户自己计算。
     */
    @Override
    protected int getCountInternal(Query query) {
        if(query.getFilter() == Filter.INCLUDE){
            Geometry geometry = getDataStore().getRecord();
            int count = geometry.getNumGeometries();
            return count;
        }
        return -1;
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) {
        return new BCGISFeatureReader(getState(), query);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() {

        logger.info("=========> buildFeatureType======" + entry.getName().toString());

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

        builder.setName(entry.getName());
        builder.setCRS(DefaultGeographicCRS.WGS84);
        BCGISDataStore bcgisDataStore = getDataStore();
        Geometry geometry = bcgisDataStore.getRecord();

        if (geometry.getNumGeometries() < 1) {
            builder.add("geom", LineString.class);
        } else {
            String geometryType = geometry.getGeometryN(0).getGeometryType().toLowerCase();
            if (geometryType.equals("point")) {
                builder.add("geom", Point.class);
            } else if(geometryType.equals("multipoint")){
                builder.add("geom",MultiPoint.class);
            }else if(geometryType.equals("linestring")){
                builder.add("geom",LineString.class);
            }else if(geometryType.equals("multilinestring")){
                builder.add("geom",MultiLineString.class);
            }else if(geometryType.contains("polygon")){
                builder.add("geom", Polygon.class);
            } else if (geometryType.contains("multipolygon")) {
                builder.add("geom", MultiPolygon.class);
            }
        }

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException{
        return super.handleVisitor(query,visitor);
        // WARNING: Please note this method is in BCGISeatureSource!
    }
}
















