package com.atlchain.bcgis.data;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKBWriter;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.NoSuchElementException;

// Iterator supporting writing of feature content

// We will be outputting content to a temporary file, leaving the original for concurrent processes such as rendering.
// When streaming is closed the temporary file is moved into the correct location to effect the change.
public class BCGISFeatureWriter implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    // State of current transaction
    private ContentState state;

    // Delegate handing reading of original file
    private BCGISFeatureReader delegate;

    // Temporary file used to stage output
    private File temp ;

    // Current feature available for modification, may be null if feature removed
    private SimpleFeature currentFeature;

    // Flag indicating we have reached the end of the file
    private boolean appending = false;

    // new add
    private WKBWriter wkbWriter;

    // Row count used to generate FeatureId when appending
    int nextRow = 0 ;

    // 定义全局变量保存每一次循环添加的geometry
    private ArrayList<Geometry> geometryArrayList = new ArrayList<>();


    //1.Setting up a temporary file for output                                    2.Creating a WkbWriter for output
    //3.Quickly making a copy of the file if we are just interested in appending  4.Creating a delegate to read the original file
    public BCGISFeatureWriter(ContentState state, Query query) throws  IOException{

        // TODO new add 增加临时文件看计算结果如何
        String typename = query.getTypeName();
        File file = ((BCGISDataStore)state.getEntry().getDataStore()).file;
        File dir = file.getParentFile();
        this.temp = File.createTempFile(typename + System.currentTimeMillis(),".wkb",dir);
//        System.out.println(this.temp); //打印输出结合测试中 t2.commit(); 可知是在提交事务的时候这里才会运行

        this.state = state;
        // 实现委托以读取文件
        this.delegate = new BCGISFeatureReader(state);
    }

    // Add FeatureWriter.getFeatureType() implementation
    @Override
    public SimpleFeatureType getFeatureType() {

        return state.getFeatureType();
    }

    // new add  making use of delegate before switching over to returning false when appending
    @Override
    public boolean hasNext() throws IOException {

        if(this.appending){
            return false;// reader has no more contents
        }
        return delegate.hasNext();
    }

    // TODO  实现 wkb 文件的写入保存为字符串
    // To access Features for modification or removal (when working through existing content) 处理现有内容时修改或删除特征值
    // To create new Features (when working past the end of the file)  当循环到文件末尾时可以增加特征feature
    @Override
    public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {

        if(this.currentFeature != null){
            this.write();// the previous one was not written, so do it now.
        }
        try{
            if(!appending){
                if(delegate.geometry != null && delegate.hasNext()){
                    this.currentFeature = delegate.next();
//                    System.out.println("======" + this.currentFeature);            // 模拟打印出看每次获取值是否存在
                    //输出结果SimpleFeatureImpl:Line=[SimpleFeatureImpl.Attribute: geom<geom id=Line.1>=MULTILINESTRING ((72.06559064066316 419.7291271211486, 180.64773629097166 419.7291271211486))]
                    //暂时只有一个属性
                    return  this.currentFeature;
                }else{
                    this.appending = true;
                }
            }
            SimpleFeatureType featureType = state.getFeatureType();
            String fid = featureType.getTypeName() + "." + nextRow;
            // defaultValues(SimpleFeatureType featureType) Produce a set of default values for the provided FeatureType
            Object values[] = DataUtilities.defaultValues(featureType);

            this.currentFeature = SimpleFeatureBuilder.build(featureType,values,fid);
            return  this.currentFeature;
        }catch (IllegalAttributeException invalid){
            throw new IOException("Unable to create feature :" + invalid.getMessage(),invalid);
        }
    }

    // Mark our {@link #currentFeature} feature as null, it will be skipped when written effectively removing it.
    @Override
    public void remove() throws IOException {
        this.currentFeature = null;
    }

    // TODO 把坐标写到文件里面   写进去之后不会返回值
    @Override
    public void write() throws IOException {
        if(this.currentFeature == null){
            return;
        }
        for(Property property:currentFeature.getProperties()){
            Object value = property.getValue();
            if(value == null){
                Geometry[] geometries = geometryArrayList.toArray(new Geometry[geometryArrayList.size()]);
                GeometryCollection geometryCollection = getGeometryCollection(geometries);
                Geometry geometry = geometryCollection;
//                System.out.println("+++==========="+ geometry.getNumGeometries()+"==============");
//                System.out.println(geometry);
                byte[] wkbByteArray = new WKBWriter().write(geometry);
                FileOutputStream out = new FileOutputStream(this.temp);
                out.write(wkbByteArray);
            }else if(value instanceof Geometry){
                Geometry geometry1 = (Geometry)value;
                geometryArrayList.add(geometry1);
            }
        }
        nextRow++;
        this.currentFeature = null ;// indicate that it has been written
    }

    // 实现空间几何对象转换
    private GeometryCollection getGeometryCollection(Geometry[] geomList) {
        GeometryFactory geometryFactory = new GeometryFactory();
        return new GeometryCollection(geomList, geometryFactory);
    }

    //  replace the existing File with our new one
    @Override
    public void close() throws IOException {

        if(this.currentFeature != null){
         this.write();
        }
        //Step 1: Write out remaining contents (if applicable)
        while (hasNext()){
            next();
            write();
        }

        if(delegate != null){
            this.delegate.close();
            this.delegate = null;
        }
//         Step 2: Replace file contents
        File file = ((BCGISDataStore)state.getEntry().getDataStore()).file;

        Files.copy(temp.toPath(),file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}

