package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {

    public BCGISDataStoreFactory() {}

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
//        File file = (File) FILE_PARAM.lookUp(params);
        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                new File(this.getClass().getResource("/certs/user/cert.pem").getPath()),
                new File(this.getClass().getResource("/certs/user/user_sk").getPath()),
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050",
                "atlchannel" ,
                "bincc",
                "GetByteArray",
                "Line"
        );
        return bcgisDataStore;
    }

    @Override
    public String getDisplayName() {
        return "BCGIS";
    }

    @Override
    public String getDescription() {
        return "WKB binary file";
    }

    public static final Param BC_PARAM =
            new Param(
                    "bcgis",
                    String.class,
                    "data from blockchain"
            );

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { BC_PARAM };
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        return DataUtilities.canProcess(params, getParametersInfo());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) {
        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                new File(this.getClass().getResource("/certs/user/cert.pem").getPath()),
                new File(this.getClass().getResource("/certs/user/user_sk").getPath()),
                "TestOrgA",
                "grpc://172.16.15.66:7051",
                "TestOrgA",
                "admin",
                "OrdererTestOrgA",
                "grpc://172.16.15.66:7050",
                "atlchannel" ,
                "bincc",
                "GetByteArray",
                "LineCreateNewDataStore"
        );
        return bcgisDataStore;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
