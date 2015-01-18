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

import org.geojson.GeoJsonObject;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.formats.conversions.to.ToHelper;


public class FromHelper
{

    private Module module;

    public FromHelper(Module module)
    {
        this.module = module;
    }

    private FromHelper()
    {
    }

    public ToHelper from(TGeometry tGeometry)
    {
        return new ToHelper(module, new TGeometryFromConverter(module).convert(tGeometry));
    }

    public ToHelper from(GeoJsonObject geoJsonObject)
    {
        return new ToHelper(module, new GeoJsonFromConverter(module).convert(geoJsonObject));

    }

    public ToHelper from(String wkt) throws Exception
    {
        return new ToHelper(module, new WKTFromConverter(module).convert(wkt, null));
    }

    public ToHelper from(String wkt, String crs) throws Exception
    {
        return new ToHelper(module, new WKTFromConverter(module).convert(wkt, crs));
    }
}
