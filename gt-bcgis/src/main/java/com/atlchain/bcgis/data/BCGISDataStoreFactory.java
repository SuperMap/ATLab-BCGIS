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
    public String getDisplayName() {
        return "BCGIS";
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
    public String getDescription() {
        return "WKB binary file";
    }

    public static final Param cert_PARAM             = new Param("cert", String.class, "cert file of user");
    public static final Param key_PARAM              = new Param("key", String.class, "key file of user");
    public static final Param peerName_PARAM         = new Param("peerName", String.class, "peerName string");
    public static final Param peerUrl_PARAM          = new Param("peerUrl", String.class, "peerUrl string ");
    public static final Param mspId_PARAM            = new Param("mspIde", String.class, "mspId string ");
    public static final Param userName_PARAM         = new Param("userName", String.class, "userName string ");
    public static final Param ordererName_PARAM      = new Param("ordererName", String.class, "ordererName string");
    public static final Param ordererUrl_PARAM       = new Param("ordererUrl", String.class, "ordererUrl string ");
    public static final Param channelName_PARAM      = new Param("channelName", String.class, "channelName string ");
    public static final Param chaincodeName_PARAM    = new Param("chaincodeName", String.class, "chaincodeName string ");
    public static final Param functionName_PARAM     = new Param("functionName", String.class, "functionName string ");
    public static final Param recordKey_PARAM        = new Param("recordKey", String.class, "recordKeyName string ");
    @Override
    public Param[] getParametersInfo() {
        return new Param[] {
                cert_PARAM ,
                key_PARAM ,
                peerName_PARAM,
                peerUrl_PARAM ,
                mspId_PARAM,
                userName_PARAM,
                ordererName_PARAM,
                ordererUrl_PARAM ,
                channelName_PARAM,
                chaincodeName_PARAM,
                functionName_PARAM,
                recordKey_PARAM };
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        String certfile = (String) cert_PARAM.lookUp(params);
        String keyfile = (String) key_PARAM.lookUp(params);
        String peerNamestring = (String) peerName_PARAM.lookUp(params);
        String peerUrlstring = (String) peerUrl_PARAM.lookUp(params);
        String mspIdstring = (String) mspId_PARAM.lookUp(params);
        String userNamestring = (String) userName_PARAM.lookUp(params);
        String ordererNamestring = (String) ordererName_PARAM.lookUp(params);
        String ordererUrlstring = (String) ordererUrl_PARAM.lookUp(params);
        String channelNametring = (String) channelName_PARAM.lookUp(params);
        String chaincodeNamestring = (String) chaincodeName_PARAM.lookUp(params);
        String functionNamestring = (String) functionName_PARAM.lookUp(params);
        String recordKeystring = (String) recordKey_PARAM.lookUp(params);

        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                new File(certfile), //new File(this.getClass().getResource("/certs/user/cert.pem").getPath()),
                new File(keyfile),//new File(this.getClass().getResource("/certs/user/user_sk").getPath()),
                peerNamestring,//"TestOrgA",
                peerUrlstring,//"grpc://172.16.15.66:7051",
                mspIdstring,//"TestOrgA",
                userNamestring,//"admin",
                ordererNamestring,//"OrdererTestOrgA",
                ordererUrlstring,//"grpc://172.16.15.66:7050",
                channelNametring,//"atlchannel" ,
                chaincodeNamestring,//"bincc",
                functionNamestring,//"GetByteArray",
                recordKeystring//"Line"
        );
        return bcgisDataStore;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
//        BCGISDataStore bcgisDataStore = new BCGISDataStore(
//                new File(this.getClass().getResource("/certs/user/cert.pem").getPath()),
//                new File(this.getClass().getResource("/certs/user/user_sk").getPath()),
//                "TestOrgA",
//                "grpc://172.16.15.66:7051",
//                "TestOrgA",
//                "admin",
//                "OrdererTestOrgA",
//                "grpc://172.16.15.66:7050",
//                "atlchannel" ,
//                "bincc",
//                "GetByteArray",
//                "LineCreateNewDataStore"
//        );
//        return bcgisDataStore;
        File certfile = (File) cert_PARAM.lookUp(params);
        String keyfile = (String) key_PARAM.lookUp(params);
        String peerNamestring = (String) peerName_PARAM.lookUp(params);
        String peerUrlstring = (String) peerUrl_PARAM.lookUp(params);
        String mspIdstring = (String) mspId_PARAM.lookUp(params);
        String userNamestring = (String) userName_PARAM.lookUp(params);
        String ordererNamestring = (String) ordererName_PARAM.lookUp(params);
        String ordererUrlstring = (String) ordererUrl_PARAM.lookUp(params);
        String channelNametring = (String) channelName_PARAM.lookUp(params);
        String chaincodeNamestring = (String) chaincodeName_PARAM.lookUp(params);
        String functionNamestring = (String) functionName_PARAM.lookUp(params);
        String recordKeystring = (String) recordKey_PARAM.lookUp(params);

        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                certfile, //new File(this.getClass().getResource("/certs/user/cert.pem").getPath()),
                new File(keyfile),//new File(this.getClass().getResource("/certs/user/user_sk").getPath()),
                peerNamestring,//"TestOrgA",
                peerUrlstring,//"grpc://172.16.15.66:7051",
                mspIdstring,//"TestOrgA",
                userNamestring,//"admin",
                ordererNamestring,//"OrdererTestOrgA",
                ordererUrlstring,//"grpc://172.16.15.66:7050",
                channelNametring,//"atlchannel" ,
                chaincodeNamestring,//"bincc",
                functionNamestring,//"GetByteArray",
                recordKeystring//"Line"
        );
        return bcgisDataStore;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
