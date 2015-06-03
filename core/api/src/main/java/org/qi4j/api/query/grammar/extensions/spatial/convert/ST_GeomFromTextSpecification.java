package org.qi4j.api.query.grammar.extensions.spatial.convert;

import org.qi4j.api.geometry.TGeometry;

public class ST_GeomFromTextSpecification<T extends TGeometry>
    extends SpatialConvertSpecification
{
    public ST_GeomFromTextSpecification( String WKT, String crs )
    {
        super( WKT, crs );
    }

    @Override
    public String toString()
    {
        return "CONVERTING ( " + geometryAsWKT + " )";
    }
}
