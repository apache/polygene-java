package org.qi4j.api.dataset;

/**
 * TODO
 */
public interface DataSetSource
{
    <T> DataSet<T> newDataSet( Class<T> type );
}
