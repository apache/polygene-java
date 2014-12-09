package org.qi4j.index.elasticsearch.extension.spatial.model;

import com.vividsolutions.jts.geom.MultiLineString;
import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.*;
import org.qi4j.api.property.Property;

/**
 * Created by jj on 01.12.14.
 */
public interface VerifyStatialTypes {

    @Optional
    Property<TPoint> point();

    @Optional
    Property<TMultiPoint> mPoint();

    @Optional
    Property<TLineString> line();

    @Optional
    Property<TPolygon> polygon();

    @Optional
    Property<TMultiPolygon> mPolygon();

    @Optional
    Property<TFeature> feature();

    @Optional
    Property<TFeatureCollection> collection();
}
