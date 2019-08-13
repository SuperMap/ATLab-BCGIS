package com.atlchain.bcgis.data;

import com.atlchain.sdk.ATLChain;

import java.io.File;

/**
 * 区块链操作类，用于和区块链进行交互
 */
public class BlockChainClient {
    private ATLChain atlChain;

    BlockChainClient(
            File certFile,
            File keyFile,
            String peerName,
            String peerUrl,
            String mspId,
            String userName,
            String ordererName,
            String ordererUrl
    )
    {
        this.atlChain = new ATLChain(
                certFile,
                keyFile,
                peerName,
                peerUrl,
                mspId,
                userName,
                ordererName,
                ordererUrl
        );
    }

    // 读取链上数据，通道名、链码名称、方法名有默认值
    public byte[][] getRecord(String recordKey) {
        return getRecord(recordKey, "atlchannel", "atlchaincc", "GetByteArray");
    }

    public byte[][] getRecord(String recordKey, String channelName) {
        return getRecord(recordKey, channelName, "atlchaincc", "GetByteArray");
    }

    public byte[][] getRecord(String recordKey, String channelName, String chaincodeName) {
        return getRecord(recordKey, channelName, chaincodeName, "GetByteArray");
    }

    public byte[][] getRecord(String recordKey, String channelName, String chaincodeName, String functionName) {
        byte[] byteKey = recordKey.getBytes();
        byte[][] result = atlChain.queryByte(
                channelName,
                chaincodeName,
                functionName,
                new byte[][]{byteKey}
        );
        return result;
    }

    // 向链上写数据，通道名、链码名称、方法名有默认值，默认写入字符串“record”
    public String putRecord(String recordKey) {
        return putRecord(recordKey, "record".getBytes(), "atlchannel", "atlchaincc", "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record) {
        return putRecord(recordKey, record, "atlchannel", "atlchaincc", "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record, String channelName) {
        return putRecord(recordKey, record, channelName, "atlchaincc", "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record, String channelName, String chaincodeName) {
        return putRecord(recordKey, record, channelName, chaincodeName, "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record, String channelName, String chaincodeName, String functionName) {
        byte[] byteKey = recordKey.getBytes();
        String result = atlChain.invokeByte(
                channelName,
                chaincodeName,
                functionName,
                new byte[][]{byteKey, record}
        );
        return result;
    }
}
