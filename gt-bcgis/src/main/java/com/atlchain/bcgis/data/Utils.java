package com.atlchain.bcgis.data;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.security.MessageDigest;
import java.util.ArrayList;
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

    public static String getGeometryStr(ArrayList<Geometry> geometryArrayList) {
        StringBuilder builder = new StringBuilder();
        for (Geometry geometry : geometryArrayList) {
            builder.append(geometry.toString());
        }
        return builder.toString();
    }

    public static String getSHA256(String str) {
        if (str == null) {
            return null;}
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(str.getBytes());
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);}
    }

    // StringBuffer 建立的字符串可以进行修改，并且不产生新的未使用对象
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
