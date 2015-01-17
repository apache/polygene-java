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

package org.qi4j.library.spatial.formats.conversions.from;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.context.jts.JtsSpatialContextFactory;
import com.spatial4j.core.io.jts.JtsWKTReaderShapeParser;
import com.spatial4j.core.io.jts.JtsWktShapeParser;
import com.spatial4j.core.shape.Circle;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.geometry.TGeometryFactory.TPoint;


public class WKTFromConverter
{


    final SpatialContext ctx;

    {
        JtsSpatialContextFactory factory = new JtsSpatialContextFactory();
        factory.srid = 4326;
        factory.datelineRule = JtsWktShapeParser.DatelineRule.ccwRect;
        factory.wktShapeParserClass = JtsWKTReaderShapeParser.class;
        ctx = factory.newSpatialContext();
    }


    private Module module;

    public WKTFromConverter(Module module)
    {
        this.module = module;
    }

    public TGeometry convert(String wkt, String crs) throws ParseException
    {

        Shape sNoDL = ctx.readShapeFromWkt(wkt);


        if (!sNoDL.hasArea())
        {
            return buildPoint(module, sNoDL);
        } else
        {

            Geometry jtsGeometry = ((JtsGeometry) sNoDL).getGeom();

            if (jtsGeometry instanceof Polygon)
            {
                return buildPolygon(module, sNoDL);
            } else if (jtsGeometry instanceof MultiPolygon)
            {
            } else if (jtsGeometry instanceof LineString)
            {
            }
        }


        if (sNoDL instanceof Point)
        {


            ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);
            builder.prototype().x(((Point) sNoDL).getX()).y(((Point) sNoDL).getY());
            return builder.newInstance();
        } else if (sNoDL instanceof Circle)
        {

        } else
        {
            if (sNoDL.hasArea()) System.out.println("Shape With area..");

            if (sNoDL instanceof JtsGeometry)
            {

            }

        }


        return null;
    }


    private TPoint buildPoint(Module module, Shape sNoDL)
    {
        ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);
        builder.prototype().x(((Point) sNoDL).getX()).y(((Point) sNoDL).getY());
        return builder.newInstance();
    }

    private TPolygon buildPolygon(Module module, Shape sNoDL)
    {

        Geometry jtsGeometry = ((JtsGeometry) sNoDL).getGeom();
        Polygon jtsPolygon = (Polygon) jtsGeometry;

        com.vividsolutions.jts.geom.Coordinate[] coordinates = jtsPolygon.getExteriorRing().getCoordinates();

        ValueBuilder<TPolygon> polygonBuilder = module.newValueBuilder(TPolygon.class);
        ValueBuilder<TLinearRing> tLinearRingBuilder = module.newValueBuilder(TLinearRing.class);

        List<TPoint> points = new ArrayList<>();
        for (int i = 0; i < coordinates.length; i++)
        {

            points.add
                    (
                            TPoint(module)

                                    .x(coordinates[i].x)
                                    .y(coordinates[i].y).geometry()
                    );
        }
        tLinearRingBuilder.prototype().of(points);


        ValueBuilder<TPolygon> builder = module.newValueBuilder(TPolygon.class);

        builder.prototype().of
                (
                        tLinearRingBuilder.newInstance()
                );

        return builder.newInstance();
    }


}
