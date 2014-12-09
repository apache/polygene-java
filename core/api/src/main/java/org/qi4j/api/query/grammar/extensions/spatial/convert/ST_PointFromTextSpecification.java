package org.qi4j.api.query.grammar.extensions.spatial.convert;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import java.text.ParseException;

/**
 * ST_Within Specification.
 *
 *
 */
public class ST_PointFromTextSpecification<TGeometry>
    extends SpatialConvertSpecification<TGeometry>
{


    final SpatialContext ctx;
    {
        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.datelineRule = JtsWktShapeParser.DatelineRule.ccwRect;
        factory.wktShapeParserClass = JtsWKTReaderShapeParser.class;
        ctx = factory.newSpatialContext();
    }


    public ST_PointFromTextSpecification(String WKT, int srid)
    {
        super( WKT, srid );

       // Shape sNoDL = ctx.readShapeFromWkt("POINT(-71.064544 42.28787)");

    }

    @Structure
    Module module;



    @Override
    public org.qi4j.api.geometry.internal.TGeometry convert(Module module) throws ParseException
    {
       // return value.equals( this.value );
        Shape sNoDL = ctx.readShapeFromWkt((property()));

        if (sNoDL instanceof Point)
        {
            System.out.println("Shape is point ");

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

        }

        System.out.println("sNoDL " + sNoDL);

        return null;
    }

    @Override
    public String toString()
    {
        return "converting"; //property.toString() + " is within " + value.toString();
    }
}
