package com.atlchain.bcgis.data;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * 工具类
 */
public class Utils {
    /**
     * 构造GeometryCollection对象
     * @param geomList 空间几何对象列表
     * @return
     */
    public static GeometryCollection getGeometryCollection(Geometry[] geomList) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return new GeometryCollection(geomList, geometryFactory);
    }
}
