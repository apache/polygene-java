package org.qi4j.library.spatial.transformator;

import org.geojson.*;
import org.qi4j.api.geometry.GeometryFactory;
import org.qi4j.api.geometry.TFeature;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TLinearRing;

import java.util.*;

/**
 * Created by jakes on 2/23/14.
 */
public class GeoJsonTransformator {

    private static GeoJsonTransformator  self;

    private GeometryFactory Geometry = null;

    public static final GeoJsonTransformator withGeometryFactory(GeometryFactory Geometry)
    {
        if (self == null)
            self = new GeoJsonTransformator();

        self.Geometry = Geometry;

        return self;
    }


    public  TFeature transformGeoFeature(Feature feature)
    {
        TFeature tFeature = null;

        if (feature.getGeometry() instanceof Point)
        {
            tFeature= Geometry.asFeature(
                    Geometry.asPoint(
                            Geometry.asCoordinate(
                                    ((org.geojson.Point)feature.getGeometry()).getCoordinates().getLatitude(),
                                    ((org.geojson.Point)feature.getGeometry()).getCoordinates().getLongitude(),
                                    ((org.geojson.Point)feature.getGeometry()).getCoordinates().getAltitude()
                            )
                    ));

        } else if (feature.getGeometry() instanceof LineString)
        {
            tFeature = Geometry.asFeature(
                    Geometry.asLinearString(toPoints(((LineString)feature.getGeometry()).getCoordinates()).toArray(new TPoint[0]))
            );
        }
        else if (feature.getGeometry() instanceof Polygon)
        {

            TLinearRing shell = toLinearRing( toPoints(((Polygon)feature.getGeometry()).getExteriorRing()));

            Set<TLinearRing> holes = new HashSet<>();
//
//                   TLinearRing shell = Geometry.asLinearRing(points.toArray(new TPoint[0]));
//
            for (int i = 0; i < ((Polygon)feature.getGeometry()).getInteriorRings().size(); i++)
            {
                holes.add(Geometry.asLinearRing(toPoints(((Polygon)feature.getGeometry()).getInteriorRing(i)).toArray(new TPoint[0])));

                // holes( toLinearRing(toPoints(((Polygon)feature.getGeometry()).getInteriorRing(i)) ) );
                // asLinearRing (
                // toPoints(((Polygon)feature.getGeometry()).getInteriorRing(i))
                // );
            }
//
//
//                   if (holes.size()> 0) {
//                       System.out.println(holes.size());
//                   } else
//                   {
//                       Geometry.asPolygon(shell); // , holes.toArray(new TLinearRing[0]));
//                   }
        }
        else if (feature.getGeometry() instanceof MultiPolygon)
        {
            //  ((MultiPolygon)feature.getGeometry()).g getCoordinates()
            ListIterator<List<List<LngLatAlt>>> a = ((MultiPolygon)feature.getGeometry()).getCoordinates().listIterator();

            while(a.hasNext()) {
                //
                a.next();
            }
        }
        else {
            System.out.println(feature.getGeometry().getClass() + " missing");
        }

        if (tFeature != null) {
            tFeature.id().set(feature.getId());
            tFeature.properties().set(feature.getProperties());

            //  System.out.println(count);

            // System.out.println(tFeature.toString());
        }

        return tFeature;


    }

    private  Set<TPoint> toPoints(List<LngLatAlt> coordinates)
    {
        // get the shell
        Iterator<LngLatAlt> shellPoints = coordinates.iterator();

        Set<TPoint> points = new HashSet<>();
        while(shellPoints.hasNext())
        {
            LngLatAlt p = shellPoints.next();
            // Geometry.asCoordinate(c.getLatitude(),c.getLongitude(), c.getAltitude() );
            TPoint tpoint = Geometry.asPoint(
                    Geometry.asCoordinate(
                            p.getLatitude(),
                            p.getLongitude(),
                            p.getAltitude()
                    )
            );

            points.add(tpoint);

            // Geometry.asLinearRing(null).
            // Geometry.
        }
        return points;
    }

    private  TLinearRing toLinearRing(Set<TPoint> points)
    {
        return Geometry.asLinearRing(points.toArray(new TPoint[0]));
    }


    private GeoJsonTransformator()
    {
    }

}
