package com.atlchain.bcgis.data;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.util.UUID;

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

    public static byte[] getBytesFromGeometry(Geometry geometry) {
        WKBWriter writer = new WKBWriter();
        byte[] bytes = writer.write(geometry);
        return bytes;
    }

    public static Geometry getGeometryFromBytes(byte[] bytes) throws ParseException {
        Geometry geometry = new WKBReader().read(bytes);
        return geometry;
    }

    public static UUID getUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid;
    }
}
