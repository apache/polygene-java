package org.qi4j.api.query.grammar.extensions.spatial.convert;

import org.qi4j.api.geometry.internal.TGeometry;


public class ST_GeomFromTextSpecification<T extends TGeometry>
        extends SpatialConvertSpecification<T>
{
    public ST_GeomFromTextSpecification(String WKT, String crs)
    {
        super(WKT, crs);
    }

    @Override
    public String toString()
    {
        return "CONVERTING ( " +  geometryAsWKT + " )";
    }
}
