package org.qi4j.api.query.grammar.extensions.spatial.convert;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

import java.text.ParseException;

/**
 * ST_Within Specification.
 *
 *
 */
public class ST_PointFromTextSpecification<T extends TGeometry>
    extends SpatialConvertSpecification<T>
{




    public ST_PointFromTextSpecification(String wkt, String crs)
    {
        super( wkt, crs );
    }

    @Structure
    Module module;





    @Override
    public String toString()
    {
        return "converting"; //property.toString() + " is within " + value.toString();
    }
}
