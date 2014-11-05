package org.qi4j.api.geometry;

import org.qi4j.api.property.Property;

import java.util.List;

/**
 * Created by jakes on 2/7/14.
 */
public interface TFeatureCollection extends TGeomRoot {


    Property<List<TFeature>> features();

}
