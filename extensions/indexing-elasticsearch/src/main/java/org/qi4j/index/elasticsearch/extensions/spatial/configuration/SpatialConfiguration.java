package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Created by jj on 22.12.14.
 */
public class SpatialConfiguration
{

    public static enum INDEXING_METHOD {USE_GEO_POINT, USE_GEO_SHAPE}

    public interface Configuration extends ValueComposite
    {
        @Optional Property<Boolean> Enabled();
        @Optional Property<IndexerConfiguration> Indexer();
        @Optional Property<FinderConfiguration>  Finder ();


    }

    public interface IndexerConfiguration extends ValueComposite
    {
        Property<INDEXING_METHOD> Method();
        @Optional Property<ProjectionSupport> Projection();
    }

    public interface FinderConfiguration extends ValueComposite
    {
        @Optional Property<ProjectionSupport> Projection();
    }


    public interface ProjectionSupport extends ValueComposite
    {
        @Optional Property<Boolean> ConversionEnabled();
        @Optional Property<String>  ConversionAccuracy();
    }


    public static boolean isEnabled(Configuration config)
    {
        return config.Enabled().get().booleanValue();
    }

    public static INDEXING_METHOD getMethod(Configuration config)
    {
        return config.Indexer().get().Method().get();
    }

    public static boolean isMethodGeoPoint(Configuration config)
    {
        return config.Indexer().get().Method().get() == INDEXING_METHOD.USE_GEO_POINT ? true : false;
    }

    public static boolean isMethodGeoShape(Configuration config)
    {
        return config.Indexer().get().Method().get() == INDEXING_METHOD.USE_GEO_SHAPE ? true : false;
    }

    public static boolean isIndexerProjectionConversionEnabled(Configuration config)
    {
        return config.Indexer().get().Projection().get().ConversionEnabled().get().booleanValue();
    }

    public static double getIndexerProjectionConversionAccuracy(Configuration config)
    {
        // return config.Indexer().get().Projection().get().
        return 2;
    }

    public static boolean isFinderProjectionConversionEnabled(Configuration config)
    {
        return config.Finder().get().Projection().get().ConversionEnabled().get().booleanValue();
    }

    public static double getFinderProjectionConversionAccuracy(Configuration config)
    {
        return 2;
    }




}
