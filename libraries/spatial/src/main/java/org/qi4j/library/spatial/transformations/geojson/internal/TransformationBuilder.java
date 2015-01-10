package org.qi4j.library.spatial.transformations.geojson.internal;

import org.geojson.*;
import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;

import java.util.List;
import java.util.ListIterator;

import static org.qi4j.api.geometry.TGeometryFactory.*;

/**
 * Created by jj on 28.11.14.
 */
public class TransformationBuilder {

    Module module;

    private GeoJsonObject geojson;

   public TransformationBuilder(Module module)
    {
        this.module = module;
    }

    public TransformationBuilder from(GeoJsonObject geojson )
    {
        this.geojson = geojson;
        return this;
    }

    public TGeometry transform()
    {

        if (geojson instanceof Feature)
        {
            Feature feature = (Feature)geojson;
            return TFeature(module).of(new TransformationBuilder(module).from(feature.getGeometry()).transform()).geometry();
        }

        else if (geojson instanceof Point)
        {
            Point point = (Point)geojson;

            return TPoint(module)
                    .x(point.getCoordinates().getLatitude())
                    .y(point.getCoordinates().getLongitude())
                    .z(point.getCoordinates().getAltitude())
                    .geometry();



        } else if (geojson  instanceof LineString)
        {
            // tFeature = Geometry.asFeature(
            //        Geometry.asLinearString(toPoints(((LineString)feature.getGeometry()).getCoordinates()).toArray(new TPoint[0]))
            //);

            LineString lineString = (LineString)geojson;

            TLineString tLineString = TLineString(module).of().geometry();
            for (LngLatAlt xyz : lineString.getCoordinates() ) {
                tLineString = TLineString(module).of(
                        TPoint(module)
                                .x(xyz.getLatitude())
                                .y(xyz.getLongitude())
                                .z(xyz.getAltitude())
                                .geometry()
                ).geometry();
            }

            return tLineString;

        }
        else if (geojson instanceof Polygon)
        {

            TPolygon tPolygon = null;

            TLinearRing ring = getRing(((Polygon) geojson).getExteriorRing());

            if (ring.isValid())
                tPolygon = TPolygon(module).shell(ring).geometry();
            else
             throw  new RuntimeException("Polygon shell not valid");

            for (int i = 0; i < ((Polygon)geojson).getInteriorRings().size(); i++)
            {
                tPolygon.withHoles(getRing(((Polygon) geojson).getInteriorRings().get(i)));
            }

            return tPolygon;

        }
        else if (geojson instanceof MultiPolygon)
        {
            //  ((MultiPolygon)feature.getGeometry()).g getCoordinates()
            ListIterator<List<List<LngLatAlt>>> a = ((MultiPolygon)geojson).getCoordinates().listIterator();

            while(a.hasNext()) {
                //
                a.next();
            }
        }
        else {
            System.out.println(geojson.getClass() + " missing");
        }
/**
        if (tFeature != null) {
            tFeature.id().set(feature.getId());
            tFeature.properties().set(feature.getProperties());

            //  System.out.println(count);

            // System.out.println(tFeature.toString());
        }
   */
        return null;

    }

    private TLinearRing getRing(List<LngLatAlt> coordinates)
    {

        TLinearRing tLinearRing = TLinearRing(module).of().geometry();
        for (LngLatAlt xyz :coordinates ) {
            tLinearRing.yx(xyz.getLatitude(), xyz.getLongitude());
        }

        if (!tLinearRing.isClosed())
        {
            tLinearRing.of(tLinearRing.getStartPoint()); // hack here - we are closing the ring
        }

        return tLinearRing;
    }
}
