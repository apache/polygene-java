/*
 * Copyright 2014 Jiri Jetmar.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.spatial.formats.conversions.to;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TCircle;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;


public class Spatial4JToConverter
{


    public static final double DATELINE = 180;
    public static final JtsSpatialContext SPATIAL_CONTEXT = JtsSpatialContext.GEO;
    public static final GeometryFactory FACTORY = SPATIAL_CONTEXT.getGeometryFactory();
    public static final GeometricShapeFactory SHAPE_FACTORY = new GeometricShapeFactory();
    protected final boolean multiPolygonMayOverlap = false;
    protected final boolean autoValidateJtsGeometry = true;
    protected final boolean autoIndexJtsGeometry = true;
    protected final boolean wrapdateline = SPATIAL_CONTEXT.isGeo();
    final SpatialContext ctx;
    {
        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.srid = 4326;
        factory.datelineRule = JtsWktShapeParser.DatelineRule.ccwRect;
        factory.wktShapeParserClass = JtsWKTReaderShapeParser.class;
        ctx = factory.newSpatialContext();
    }
    private Module module;

    public Spatial4JToConverter(Module module)
    {
        this.module = module;
    }

    public GeoJsonObject convert(TGeometry intemediate)
    {
        // return transform(intemediate);
        return null;
    }

    private Shape transform(TGeometry intermediate)
    {

        switch (intermediate.getType())
        {
            case POINT:
                return null; // return newPoint((TPoint) intemediate);
            case MULTIPOINT:
                return null;
            case LINESTRING:
                return null;
            case MULTILINESTRING:
                return null;
            case POLYGON:
                return null;
            case MULTIPOLYGON:
                return null;
            case FEATURE:
                return null;
            case FEATURECOLLECTION:
                return null;
        }

        if (intermediate instanceof TCircle)
        {
            return newCircle((TCircle) intermediate);
        } else
            return null;


    }

    private Point newPoint(TPoint point)
    {

        return new Point(point.x(), point.y());
    }

    private Circle newCircle(TCircle circle)
    {
        return ctx.makeCircle(circle.getCentre().x(), circle.getCentre().y(), circle.radius().get());
    }

}
