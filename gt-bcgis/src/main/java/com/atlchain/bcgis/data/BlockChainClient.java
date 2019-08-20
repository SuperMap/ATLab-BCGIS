package com.atlchain.bcgis.data;

import com.atlchain.sdk.ATLChain;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;

import java.io.File;
import java.io.IOException;

/**
 * 区块链操作类，用于和区块链进行交互
 */
public class BlockChainClient {
    private ATLChain atlChain;

    BlockChainClient(File networkConfigFile) {
        this.atlChain = new ATLChain(networkConfigFile);
    }

    // 读取链上数据，通道名、链码名称、方法名有默认值
    public byte[][] getRecord(String recordKey) {
        return getRecord(recordKey, "atlchaincc", "GetByteArray");
    }

    public byte[][] getRecord(String recordKey, String chaincodeName) {
        return getRecord(recordKey, chaincodeName, "GetByteArray");
    }

    public byte[][] getRecord(String recordKey, String chaincodeName, String functionName) {
        byte[] byteKey = recordKey.getBytes();
        byte[][] result = atlChain.queryByte(
                chaincodeName,
                functionName,
                new byte[][]{byteKey}
        );
        return result;
    }

    // 向链上写数据，通道名、链码名称、方法名有默认值，默认写入字符串“record”
    public String putRecord(String recordKey) {
        return putRecord(recordKey, "record".getBytes(), "atlchaincc", "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record) {
        return putRecord(recordKey, record, "atlchaincc", "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record, String chaincodeName) {
        return putRecord(recordKey, record, chaincodeName, "PutByteArray");
    }

    public String putRecord(String recordKey, byte[] record, String chaincodeName, String functionName) {
        byte[] byteKey = recordKey.getBytes();
        String result = atlChain.invokeByte(
                chaincodeName,
                functionName,
                new byte[][]{byteKey, record}
        );
        return result;
    }
}
