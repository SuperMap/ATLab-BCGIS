package com.atlchain.bcgis.data;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;

public class BCGISFeatureSource extends ContentFeatureSource {
    private Geometry geometry;

    public BCGISFeatureSource(ContentEntry entry, Query query, Geometry geometry) {
        super(entry, query);
        this.geometry = geometry;
    }

    public BCGISDataStore getDataStore() {
        return (BCGISDataStore) super.getDataStore();
    }

    // 确定地图显示时的边界
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        FeatureCollection featureCollection = getFeatures();
        FeatureIterator iterator = featureCollection.features();
        ReferencedEnvelope env = DataUtilities.bounds(iterator);
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
            int count = this.geometry.getNumGeometries();
            return count;
        }
        return -1;
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) {
        return new BCGISFeatureReader(getState(), geometry);
    }

    // WKB中只有空间几何数据，没有其他属性信息，所以FeatureType中只有一个“geom”字段。
    @Override
    protected SimpleFeatureType buildFeatureType() {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());
        builder.setCRS(DefaultGeographicCRS.WGS84);
        if (geometry.getNumGeometries() < 1) {
            builder.add("geom", LineString.class);
        } else {
            String geometryType = geometry.getGeometryN(0).getGeometryType().toLowerCase();
            if (geometryType.contains("linestring")) {
                builder.add("geom", LineString.class);
            } else if (geometryType.contains("point")) {
                builder.add("geom", Point.class);
            } else if (geometryType.contains("polygon")) {
                builder.add("geom", Polygon.class);
            }
        }

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException{
        return super.handleVisitor(query,visitor);
        // WARNING: Please note this method is in CSVFeatureSource!
    }
}

