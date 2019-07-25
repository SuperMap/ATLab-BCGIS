package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.BasicFeatureTypes;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;

public class BCGISFeatureSource extends ContentFeatureSource {

    public BCGISFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);
    }

    public BCGISDataStore getDataStore() {
        return (BCGISDataStore) super.getDataStore();
    }

    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return null;
    }

    /**
     * 根据查询条件查询属性条数
     * @param query 查询条件
     * @return 符合条件的属性条数，-1则表示不能计算该条件的数量，需要外部用户自己计算。
     * @throws IOException
     */
    @Override
    protected int getCountInternal(Query query) throws IOException {
        // TODO 1个WKB文件中只包含一个Feature，如果替换其他按数据格式则需要重新实现计数方法

        return 1;
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return new BCGISFeatureReader(getState(), query);
    }

    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());
        Geometry geometry = getDataStore().read();
        if (geometry == null) {
            throw new IOException("WKB file not available");
        }
        builder.setCRS(DefaultGeographicCRS.WGS84);
        String type = getGeometryTypeInGeometryCollection(geometry);

        // 根据不同的空间几何数据类型定义属性的数据类型
        switch (type) {
            case "Point":
            case "MultiPoint":
                builder.add("geom", BasicFeatureTypes.POINT.getClass());
                break;
            case "LineString":
            case "MultiLineString":
                builder.add("geom", BasicFeatureTypes.LINE.getClass());
                break;
            case "Polygon":
            case "MultiPolygon":
                builder.add("geom", BasicFeatureTypes.POLYGON.getClass());
                break;
            default:
                break;
        }

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    private String getGeometryTypeInGeometryCollection(Geometry geometry) {
        GeometryCollectionIterator geometryCollectionIterator = new GeometryCollectionIterator(geometry);
        geometryCollectionIterator.next();
        Geometry geom = (Geometry) geometryCollectionIterator.next();
        return geom.getGeometryType();
    }
}
