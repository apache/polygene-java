package org.qi4j.api.query.grammar.extensions.spatial.convert;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.spatial4j.core.shape.jts.JtsPoint;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import static org.qi4j.api.geometry.TGEOM.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ST_Within Specification.
 *
 */
public class ST_GeomFromTextSpecification<TGeometry>
    extends SpatialConvertSpecification<TGeometry>
{

    final SpatialContext ctx;
    {
        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.srid = 4326;
        factory.datelineRule = JtsWktShapeParser.DatelineRule.ccwRect;
        factory.wktShapeParserClass = JtsWKTReaderShapeParser.class;
        ctx = factory.newSpatialContext();
    }


    public ST_GeomFromTextSpecification(String WKT, int srid)
    {
        super( WKT, srid );

       // Shape sNoDL = ctx.readShapeFromWkt("POINT(-71.064544 42.28787)");

    }

    // @Structure
    // Module module;



    @Override
    public org.qi4j.api.geometry.internal.TGeometry convert(Module module) throws ParseException
    {


       // return value.equals( this.value );
        Shape sNoDL = ctx.readShapeFromWkt((property()));

        // System.out.println("Its a JtsGeometry " + ((JtsGeometry) sNoDL).getGeom().getGeometryType());

        if (!sNoDL.hasArea()) {
            System.out.println("Its a JtsGeometry " + ((JtsPoint) sNoDL).getGeom().getGeometryType());
            return buildPoint(module,sNoDL);
        } else {
            System.out.println("Its a JtsGeometry " + ((JtsGeometry) sNoDL).getGeom().getGeometryType());

            Geometry jtsGeometry = ((JtsGeometry) sNoDL).getGeom();

            if (jtsGeometry instanceof Polygon) {
              System.out.println("Polygon");
                return buildPolygon(module,sNoDL);
            }
            else if (jtsGeometry instanceof MultiPolygon) {
                System.out.println("MultiPolygon");
            }
            else if (jtsGeometry instanceof LineString) {
                System.out.println("LineString");
            }




        }



        if (sNoDL instanceof Point)
        {
           // System.out.println("Shape is point ");

            ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);

            builder.prototype().x(((Point) sNoDL).getX()).y(((Point) sNoDL).getY());
/**
            builder.prototype().of
                    (
                            module.newValueBuilder(Coordinate.class).prototype().of(((Point) sNoDL).getX()),  //x
                            module.newValueBuilder(Coordinate.class).prototype().of(((Point) sNoDL).getY())   //y
                    );
*/
            return builder.newInstance();


//          ValueBuilder<TGeomPoint> builder = module.newValueBuilder( TGeomPoint.class );
//          TGeomPoint proto = builder.prototype();
//          List<Double> coordinates = new ArrayList<Double>();
////
//          Double lat =  3.138722;  // 3.138722;// Double.parseDouble(query.nextToken());
//          Double lon =  101.386849; // Double.parseDouble(query.nextToken());
////
////
//          coordinates.add(3.138722);
//          coordinates.add(101.386849);
//          proto.coordinates().set(coordinates);
//
//          return builder.newInstance();

        } else

        if (sNoDL instanceof Circle) {

        } else
        {
            System.out.println("Its a shape");
            if (sNoDL.hasArea() ) System.out.println("Shape With area..");

            if (sNoDL instanceof JtsGeometry) {

                System.out.println("Its a JtsGeometry " + ((JtsGeometry) sNoDL).getGeom().getGeometryType());
                // ((JtsGeometry) sNoDL).getGeom()
            }

        }

        System.out.println("sNoDL " + sNoDL);

        return null;
    }

    private TPoint buildPoint(Module module, Shape sNoDL) {
        ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);


        builder.prototype().x(((Point) sNoDL).getX()).y(((Point) sNoDL).getY());
        /**
        builder.prototype().of
                (
                        module.newValueBuilder(Coordinate.class).prototype().of(((Point) sNoDL).getX()),  //x
                        module.newValueBuilder(Coordinate.class).prototype().of(((Point) sNoDL).getY())   //y
                );
        */

        return builder.newInstance();
    }

    private TPolygon buildPolygon(Module module, Shape sNoDL) {

        Geometry jtsGeometry = ((JtsGeometry) sNoDL).getGeom();
        Polygon jtsPolygon = (Polygon)jtsGeometry;

        // Polygon jtsPolygon = ((JtsGeometry) sNoDL).getGeom();

        // Polygon jtsPolygon = (Polygon)sNoDL;

        System.out.println("Get Coordinates " + jtsPolygon.getExteriorRing().getCoordinates() );

        com.vividsolutions.jts.geom.Coordinate[] coordinates = jtsPolygon.getExteriorRing().getCoordinates();

        ValueBuilder<TPolygon> polygonBuilder = module.newValueBuilder(TPolygon.class);
        ValueBuilder<TLinearRing> tLinearRingBuilder  = module.newValueBuilder(TLinearRing.class);

        List<TPoint> points = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i++) {
/**
            TPoint point = module.newValueBuilder(TPoint.class).prototype().of
                    (
                            module.newValueBuilder(Coordinate.class).prototype().of( coordinates[i].x),  //x
                            module.newValueBuilder(Coordinate.class).prototype().of( coordinates[i].y)   //y
                    );*/
            points.add
            (
            TPOINT(module)

                    .x(coordinates[i].x)
                    .y(coordinates[i].y).geometry()
            );
        }
        tLinearRingBuilder.prototype().of(points);

        // tLinearRingBuilder.prototype().of(points);
        // tLinearRingBuilder.prototype().type()

        // TLinearRing tLinearRing = tLinearRingBuilder.newInstance();
        // System.out.println("tLinearRing .." + tLinearRing);


        ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);

        builder.prototype().of
                (
                        // tLinearRingBuilder.newInstance().of(points)
                        tLinearRingBuilder.newInstance()
                );

        return builder.newInstance();
    }


    @Override
    public String toString()
    {
        return "converting"; //property.toString() + " is within " + value.toString();
    }
}
