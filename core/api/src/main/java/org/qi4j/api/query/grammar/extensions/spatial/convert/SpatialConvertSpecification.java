package org.qi4j.api.query.grammar.extensions.spatial.convert;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.ExpressionSpecification;


public abstract class SpatialConvertSpecification<T>
        extends ExpressionSpecification
{
    protected String geometryAsWKT;
    protected TGeometry geometry;
    protected String crs;


    public SpatialConvertSpecification(String wkt, String crs)
    {
        this.geometryAsWKT = wkt;
        this.crs = crs;
    }


    public String property()
    {
        return geometryAsWKT;
    }

    public TGeometry getGeometry()
    {
        return this.geometry;
    }

    public void setGeometry(TGeometry geometry)
    {
        this.geometry = geometry;
    }

    @Override
    public final boolean satisfiedBy(Composite item)
    {
        return true;
    }

}
