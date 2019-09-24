# 地图服务相关 API 说明

## WMS

1.上传数据：

```
POST http://{ServiceURL}/bcgis/data/upload

params：
```

2.查看已发布的地图：

```
GET http://{ServiceURL}/bcgis/mapservice/wms/list/{WorkspaceName}/{DatastoreName}

params：
    - ServiceURL：服务地址
    - WorkspaceName：工作空间名称
    - DatastoreName：数据存储名称
```

返回值： Json

3.发布地图图层：

```
POST http://{ServiceURL}/bcgis/mapservice/wms/publish/

params：
    - ServiceURL：服务地址
    - WorkspaceName：工作空间名称
    - DatastoreName：数据存储名称
    - LayerName：地图图层名称
    - DataKey：区块链中地图数据的键
```

返回值： Json

4.删除地图图层：

```
GET http://{ServiceURL}/bcgis/mapservice/wms/list/{WorkspaceName}/{DatastoreName}/{LayerName}

params：
    - ServiceURL：服务地址
    - WorkspaceName：工作空间名称
    - DatastoreName：数据存储名称
    - LayerName： 地图图层名称
```

## 空间数据处理

1.缓冲区分析

2.创建空间索引

3.空间查询
