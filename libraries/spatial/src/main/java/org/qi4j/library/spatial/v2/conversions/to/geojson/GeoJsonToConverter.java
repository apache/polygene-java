package org.qi4j.library.spatial.v2.conversions.to.geojson;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.Point;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeomRoot;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;

/**
 * Created by jj on 04.12.14.
 */
public class GeoJsonToConverter<T> {


    private Module module;

    public GeoJsonToConverter(Module module)
    {
        this.module = module;
    }

    public GeoJsonObject convert(TGeometry intemediate)
    {
        return transform(intemediate);
    }

    private GeoJsonObject transform(TGeometry intemediate)
    {

        switch(intemediate.getType())
        {
            case POINT       : return buildPoint((TPoint)intemediate);
            case MULTIPOINT  : return null;
            case LINESTRING       : return null;
            case MULTILINESTRING  : return null;
            case POLYGON       : return null;
            case MULTIPOLYGON  : return null;
            case FEATURE       : return null;
            case FEATURECOLLECTION  : return null;
            default : throw new RuntimeException("Unknown TGeometry Type.");
        }

    }

    private Point buildPoint(TPoint point)
    {
        System.out.println("point.x() " + point.x());
        System.out.println("point.y() " + point.y());
        return new Point(point.x(), point.y());
    }

}
