package com.atlchain.bcgis.data;

import com.atlchain.sdk.ATLChain;
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
import java.util.Collections;
import java.util.List;

public class BCGISDataStore extends ContentDataStore {
    protected File file;

    public BCGISDataStore(File file) {
        this.file = file;
    }

    Geometry getRecord() {
        File certFile = new File(this.getClass().getResource("/certs/user/cert.pem").getPath());
        File skFile = new File(this.getClass().getResource("/certs/user/user_sk").getPath());

        ATLChain atlChain = new ATLChain(
                certFile,
                skFile,
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050"
        );

        byte[] byteKey =  "bytekey".getBytes();
        byte[][] result = atlChain.queryByte(
                "atlchannel",
                "bincc",
                "GetByteArray",
                new byte[][]{byteKey}
        );
        Geometry geometry = null;
        try {
            geometry = new WKBReader().read(result[0]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        geometry.getNumGeometries();
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
        if(file.canWrite()){
            return new BCGISFeatureStore(entry,Query.ALL);
        }else{
            return new BCGISFeatureSource(entry,Query.ALL);
        }    }
}