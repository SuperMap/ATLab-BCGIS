package com.atlchain.bcgis.data;


import org.geotools.data.*;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import java.io.IOException;

// implements FeatureStore, which extends FeatureSource
// It extends the base class ContentFeatureStore, which handles most of the heavy lifting use a delegate BCGISFeatureSource
// 进入wkb文件进行读写操作
// 和  BCGISFeatureSource 建立委托关系，即可以使用BCGISFeatureSource里面的方法和属性
public class BCGISFeatureStore extends ContentFeatureStore {

    public BCGISFeatureStore(ContentEntry entry, Query query){

        super(entry, query);
    }

    // new add  Transaction and Event Notification are handled by wrappers applied to our BCGISFeatureWriter
    // CSVFeatureStore implementations 供内部使用
    @Override
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(
            Query query, int flags) throws IOException {

        return  new BCGISFeatureWriter(getState(),query);
    }

    // new add   set up delegate and ensure both use the same Transaction(参数请求).
    // 设置FeatureSource的委托（delegate）  原因是java不能多继承
    // 在委托模式中，有两个对象参与处理同一个请求，接受请求的对象将请求委托给另一个对象来处理。委托模式使得我们可以用聚合来替代继承，它还使我们可以模拟mixin
    BCGISFeatureSource delegate =
            new BCGISFeatureSource(entry,query){
                @Override
                public void setTransaction(Transaction transaction){
                    super.setTransaction(transaction);
                    BCGISFeatureStore.this.setTransaction(
                            transaction); // Keep these two implementations on the same transaction
                }
            };
    // new add
    @Override
    public void setTransaction(Transaction transaction){
        super.setTransaction(transaction);
        if (delegate.getTransaction() != transaction){
            delegate.setTransaction(transaction);
        }
    }

    //  new add    Use the delegate to implement the internal ContentDataStore methods
    //              Implement FeatureSource methods using CSVFeatureSource implementation
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return delegate.buildFeatureType();
    }
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
        return delegate.getBoundsInternal(query);
    }

    @Override
    protected int getCountInternal(Query query) throws IOException {
        return delegate.getCountInternal(query);
    }

    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        return delegate.getReaderInternal(query);
    }

    // 这个和 BCGISFestureSource最后的委托是一致的可相互调用
    // Make handleVisitor package visible allowing BCGISFeatureStore to delegate to this implementation.
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException {
        return delegate.handleVisitor(query, visitor);
    }

    //  use the delegate to implement FeatureSource methods.
    //  Public Delegate Methods Implement FeatureSource methods using CSVFeatureSource implementation
    @Override
    public BCGISDataStore getDataStore() {

        return delegate.getDataStore();
    }

    @Override
    public ContentEntry getEntry() {

        return delegate.getEntry();
    }

    public Transaction getTransaction() {

        return delegate.getTransaction();
    }

    public ContentState getState() {

        return delegate.getState();
    }

    public ResourceInfo getInfo() {

        return delegate.getInfo();
    }

    public Name getName()
    {

        return delegate.getName();
    }

    public QueryCapabilities getQueryCapabilities() {

        return delegate.getQueryCapabilities();
    }
}
