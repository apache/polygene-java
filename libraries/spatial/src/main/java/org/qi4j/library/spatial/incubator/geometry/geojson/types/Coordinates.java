package org.qi4j.library.spatial.incubator.geometry.geojson.types;

import org.qi4j.api.property.Property;

import java.util.List;

/**
 * Created by jakes on 2/7/14.
 */
public interface Coordinates {

    Property<List<List<Double>>> coordinates();

}
