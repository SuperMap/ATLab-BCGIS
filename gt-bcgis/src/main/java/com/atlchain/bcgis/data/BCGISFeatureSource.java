package com.atlchain.bcgis.data;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;

public class BCGISFeatureSource extends ContentFeatureSource {

    public BCGISFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);
    }

    public BCGISDataStore getDataStore() {
        return (BCGISDataStore) super.getDataStore();
    }

    // TODO
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) {
        return null;
    }

    /**
     * 根据查询条件查询属性条数
     * @param query 查询条件
     * @return 符合条件的属性条数，-1则表示不能计算该条件的数量，需要外部用户自己计算。
     */
    @Override
    protected int getCountInternal(Query query) {
        if(query.getFilter() == Filter.INCLUDE){
            Geometry gemotry = getDataStore().getRecord();

            int count = gemotry.getNumGeometries();
            return count;
        }
        return -1;
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) {
        return new BCGISFeatureReader(getState());
    }

    // WKB中只有空间几何数据，没有其他属性信息，所以FeatureType中只有一个“geom”字段。
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());
        Geometry geometry = getDataStore().getRecord();

        if (geometry == null) {
            throw new IOException("WKB file not available");
        }
        builder.setCRS(DefaultGeographicCRS.WGS84);
        builder.add("geom", Geometry.class);

        final SimpleFeatureType SCHEMA = builder.buildFeatureType();
        return SCHEMA;
    }

    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException{
        return super.handleVisitor(query,visitor);
        // WARNING: Please note this method is in CSVFeatureSource!
    }
}
