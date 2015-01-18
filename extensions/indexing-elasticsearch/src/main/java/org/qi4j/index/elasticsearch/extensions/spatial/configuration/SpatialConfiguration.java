/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.elasticsearch.extensions.spatial.configuration;

import org.qi4j.api.common.Optional;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;


public class SpatialConfiguration
{

    public static boolean isEnabled(Configuration config)
    {
        return config.Enabled().get().booleanValue();
    }

    public static INDEXING_METHOD getMethod(Configuration config)
    {
        return config.Indexer().get().Method().get().Type().get();
    }

    public static boolean isMethodGeoPoint(Configuration config)
    {
        return config.Indexer().get().Method().get().Type().get() == INDEXING_METHOD.GEO_POINT ? true : false;
    }

    public static boolean isMethodGeoShape(Configuration config)
    {
        return config.Indexer().get().Method().get().Type().get() == INDEXING_METHOD.GEO_SHAPE ? true : false;
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

    public static String getIndexerPrecision(Configuration config)
    {
        return config.Indexer().get().Method().get().Precision().get();
    }

    public static boolean isFinderProjectionConversionEnabled(Configuration config)
    {
        return config.Finder().get().Projection().get().ConversionEnabled().get().booleanValue();
    }

    public static double getFinderProjectionConversionAccuracy(Configuration config)
    {
        return 2;
    }

    public String getMethodAccuracy(Configuration config)
    {
        return config.Indexer().get().Method().get().Precision().get();
    }

    public static enum INDEXING_METHOD
    {
        GEO_POINT, GEO_SHAPE
    }

    public interface Configuration extends ValueComposite
    {
        @Optional
        Property<Boolean> Enabled();

        @Optional
        Property<IndexerConfiguration> Indexer();

        @Optional
        Property<FinderConfiguration> Finder();
    }

    public interface IndexerConfiguration extends ValueComposite
    {
        @Optional
        Property<IndexingMethod> Method();

        @Optional
        Property<ProjectionSupport> Projection();
    }

    public interface FinderConfiguration extends ValueComposite
    {
        @Optional
        Property<ProjectionSupport> Projection();
    }

    public interface IndexingMethod extends ValueComposite
    {
        @Optional
        Property<INDEXING_METHOD> Type();

        @Optional
        Property<String> Precision();
    }

    public interface ProjectionSupport extends ValueComposite
    {
        @Optional
        Property<Boolean> ConversionEnabled();

        @Optional
        Property<String> ConversionAccuracy();
    }


}
