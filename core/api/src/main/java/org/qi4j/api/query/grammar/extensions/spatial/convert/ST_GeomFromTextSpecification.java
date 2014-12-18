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
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import static org.qi4j.api.geometry.TGeometryFactory.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * ST_Within Specification.
 *
 */
public class ST_GeomFromTextSpecification<T extends TGeometry>
    extends SpatialConvertSpecification<T>
{
    public ST_GeomFromTextSpecification(String WKT, String crs)
    {
        super( WKT, crs );
    }

    @Override
    public String toString()
    {
        return "converting"; //property.toString() + " is within " + value.toString();
    }
}
