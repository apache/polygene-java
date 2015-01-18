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

import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;


public class GeoJsonToConverter
{


    private Module module;

    public GeoJsonToConverter(Module module)
    {
        this.module = module;
    }

    public GeoJsonObject convert(TGeometry intemediate)
    {
        return transform(intemediate);
    }

    private GeoJsonObject transform(TGeometry intemediate)
    {

        switch (intemediate.getType())
        {
            case POINT:
                return buildPoint((TPoint) intemediate);
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
            default:
                throw new RuntimeException("Unknown TGeometry Type.");
        }

    }

    private Point buildPoint(TPoint point)
    {
        return new Point(point.x(), point.y());
    }

}
