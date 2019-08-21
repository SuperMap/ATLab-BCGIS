package com.atlchain.bcgis.data;

import com.google.common.io.Files;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BCGISDataStore extends ContentDataStore {
    private File networkConfigFile;

    private String chaincodeName;
    private String functionName;
    private String recordKey;

    public BCGISDataStore(
            File networkConfigFile,
            String chaincodeName,
            String functionName,
            String recordKey
    )
    {
        this.networkConfigFile = networkConfigFile;
        this.chaincodeName = chaincodeName;
        this.functionName = functionName;
        this.recordKey = recordKey;
    }

    public String putDataOnBlockchain(File shpFile) throws IOException, InterruptedException {
        String fileName = shpFile.getName();
        String ext = Files.getFileExtension(fileName);
        if(!"shp".equals(ext)) {
            throw new IOException("Only accept shp file");
        }

        String result = "";
        BlockChainClient client = new BlockChainClient(networkConfigFile);

        Shp2Wkb shp2WKB = new Shp2Wkb(shpFile);
        ArrayList<Geometry> geometryArrayList = shp2WKB.getGeometry();
        String key = fileName.substring(0, fileName.lastIndexOf('.'));
        if (key == "") {
            throw new IOException("Cannot get prefix filename");
        }

        byte[] bytes = null;
        result = client.putRecord(
                key,
                bytes,
                chaincodeName,
                "PutByteArray"
        );

        int index = 0;
        for (Geometry geo : geometryArrayList) {
            byte[] geoBytes = Utils.getBytesFromGeometry(geo);
            String recordKey = key + "-" + index;
            result = client.putRecord(
                    recordKey,
                    geoBytes,
                    chaincodeName,
                    "PutByteArray"
            );
            index++;
            System.out.println(result);
//            Thread.sleep(1000);
        }
        return "result";
    }

    private Geometry getRecord() {
        BlockChainClient client = new BlockChainClient(networkConfigFile);

        byte[][] result = client.getRecord(
                this.recordKey,
                this.chaincodeName,
                this.functionName
        );
        Geometry geometry = null;
        try {
            geometry = new WKBReader().read(result[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        geometry.getNumGeometries();

        if (geometry == null) {
            try {
                throw new IOException("Blockchain record is not available");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return geometry;
    }

    @Override
    protected List<Name> createTypeNames() {
        // TODO 查询所有安装的链码，返回链码名列表。以链码名为 typenames。
        // List<String>  chaincodes = getChaincodeList();
        // return chaincodes;

        // 暂时以一个固定的名字作为 TypeName
        Name typeName = new NameImpl("tmpTypeName");
        return Collections.singletonList(typeName);
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) {
        return new BCGISFeatureStore(entry, Query.ALL, getRecord());
    }
}