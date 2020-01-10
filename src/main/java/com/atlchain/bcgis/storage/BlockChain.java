package com.atlchain.bcgis.storage;

import com.alibaba.fastjson.JSONArray;
import com.atlchain.sdk.ATLChain;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * 区块链操作类，用于和区块链进行交互
 */
public class BlockChain {
    Logger logger = Logger.getLogger(BlockChain.class.toString());
    private ATLChain atlChain;

    public BlockChain(File networkConfigFile) {
        atlChain = new ATLChain(networkConfigFile);
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // 读取链上数据，通道名、链码名称、方法名有默认值
    public byte[][] getRecordBytes(String recordKey) {

        return getRecordBytes(recordKey, "atlchaincc", "GetByteArray");
    }

    public byte[][] getRecordBytes(String recordKey, String chaincodeName) {

        return getRecordBytes(recordKey, chaincodeName, "GetByteArray");
    }

    public byte[][] getRecordBytes(String recordKey, String chaincodeName, String functionName) {
        byte[] byteKey = recordKey.getBytes();
        byte[][] result = atlChain.queryByte(
                chaincodeName,
                functionName,
                new byte[][]{byteKey}
        );
        return result;
    }

    public String getRecord(String recordKey, String chaincodeName) {
        return getRecord(recordKey, chaincodeName, "GetRecordByKey");
    }

    public String getRecord(String recordKey, String chaincodeName, String functionName) {
        String key = recordKey;
        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{key}
        );
        return result;
    }

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//    // 根据范围读取数据，范围按字典顺序排序
//    public byte[][] getRecordByRange(String recordKey, String chaincodeName) {
//        String startKey = recordKey + "-0";
//        String endKey = recordKey + "-99999";
//
//        byte[][] result = atlChain.queryByte(
//                chaincodeName,
//                "GetRecordByKeyRange",
//                new byte[][]{startKey.getBytes(), endKey.getBytes()}
//        );
//        return result;
//    }
    // 2019.12.19根据提示的范围进行范围查询（startkey包含------endkey不包含）
    public byte[][] getRecordByRange(String recordKey, String chaincodeName, JSONArray jsonArray) {

        int tempRang = jsonArray.get(jsonArray.size() - 1).toString().length() + 2;
        byte[][] byteMerger = null;
        String startKey;
        String endKey;
        for(int i = 0; i < jsonArray.size() -1; i ++){
            startKey = recordKey + "-" + String.format("%0" + tempRang + "d", jsonArray.get(i));
            endKey = recordKey + "-" + String.format("%0" + tempRang + "d", Integer.parseInt(jsonArray.getString(i + 1)));
            byte[][] result = atlChain.queryByte(
                    chaincodeName,
                    "GetRecordByKeyRange",
                    new byte[][]{startKey.getBytes(), endKey.getBytes()}
            );
            byteMerger = byteMerger(byteMerger, result, i);
            result = null;
        }
        logger.info("读取数据成功");
        return byteMerger;
    }

    public static byte[][] byteMerger(byte[][] bt1, byte[][] bt2, int count){
        byte[][] byteMerger = null;
        if(count == 0){
            byteMerger = bt2;
        }else{
            byteMerger = new byte[bt1.length+bt2.length][];
            System.arraycopy(bt1, 0, byteMerger, 0, bt1.length);
            System.arraycopy(bt2, 0, byteMerger, bt1.length, bt2.length);
        }
        return byteMerger;
    }

    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    // 向链上写数据，通道名、链码名称、方法名有默认值，默认写入字符串“record”
    public String putRecord(String recordKey, byte[] record) {
        return putRecord(recordKey, record, "bincc", "PutRecordBytes");
    }

    public String putRecord(String recordKey, byte[] record, String chaincodeName) {
        return putRecord(recordKey, record, chaincodeName, "PutRecordBytes");
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

    public String putRecord(String recordKey, String record) {
        return putRecord(recordKey, record, "bincc", "PutRecordBytes");
    }

    public String putRecord(String recordKey, String record, String chaincodeName) {
        return putRecord(recordKey, record, chaincodeName, "PutRecordBytes");
    }

    public String putRecord(String recordKey, String record, String chaincodeName, String functionName) {
        String result = atlChain.invoke(
                chaincodeName,
                functionName,
                new String[]{recordKey, record}
        );
        return result;
    }
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    public String getRecordBySeletorByPage(String chaincodeName, String functionName, String selector, String page, String bookMark) {
        String result = atlChain.query(
                chaincodeName,
                functionName,
                new String[]{selector, page, bookMark}
        );
        return result;
    }
}
