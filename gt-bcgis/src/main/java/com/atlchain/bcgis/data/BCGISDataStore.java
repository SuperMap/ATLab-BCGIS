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

// 目前只支持以明确指定BCGISDataStore的方式使用，不支持DataStoreFinder的方式根据Param自动搜索。
public class BCGISDataStore extends ContentDataStore {
    File file;

    private File certFile;
    private File keyFile;
    private String peerName;
    private String peerUrl;
    private String mspId;
    private String userName;
    private String ordererName;
    private String ordererUrl;
    private String channelName;
    private String chaincodeName;
    private String functionName;
    private String recordKey;

    public BCGISDataStore(File certFile,
                          File keyFile,
                          String peerName,
                          String peerUrl,
                          String mspId,
                          String userName,
                          String ordererName,
                          String ordererUrl,
                          String channelName,
                          String chaincodeName,
                          String functionName,
                          String recordKey
                          )
    {
        this.certFile = certFile;
        this.keyFile = keyFile;
        this.peerName = peerName;
        this.peerUrl = peerUrl;
        this.mspId = mspId;
        this.userName = userName;
        this.ordererName = ordererName;
        this.ordererUrl = ordererUrl;
        this.channelName = channelName;
        this.chaincodeName = chaincodeName;
        this.functionName = functionName;
        this.recordKey = recordKey;
    }

    private Geometry getRecord() {
        ATLChain atlChain = new ATLChain(
                this.certFile,
                this.keyFile,
                this.peerName,
                this.peerUrl,
                this.mspId,
                this.userName,
                this.ordererName,
                this.ordererUrl
        );

        byte[] byteKey = this.recordKey.getBytes();
        byte[][] result = atlChain.queryByte(
                this.channelName,
                this.chaincodeName,
                this.functionName,
                new byte[][]{byteKey}
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
        // TODO 如何判断是否可写入
        if(true){
            return new BCGISFeatureStore(entry, Query.ALL, getRecord());
        }else{
            return new BCGISFeatureSource(entry, Query.ALL, getRecord());
        }
    }
}