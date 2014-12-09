package org.qi4j.api.geometry.internal;

import org.qi4j.api.value.ValueComposite;

/**
 * Created by jakes on 2/7/14.
 */
public interface TGeomRoot extends ValueComposite {

   // public enum GeoType {GEOMETRY, FEATURE, FEATURECOLLECTION}

    public static enum TGEOM_2D {POINT,POLYGON, GEOMETRY, FEATURE, FEATURECOLLECTION}

    public static enum TGEOMETRY {POINT, MULTIPOINT, LINESTRING, MULTILINESTRING, POLYGON, MULTIPOLYGON, FEATURE, FEATURECOLLECTION}


    //  public GeoType getGeoType();

    // @Name("Point");
}
