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
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.structure.Module;

import static org.qi4j.library.spatial.projections.transformations.TTransformations.Transform;

/**
 * Created by jj on 04.12.14.
 */
public class ToHelper
{

    private Module module;
    private TGeometry intermediate;

    public ToHelper(Module module, TGeometry intermediate)
    {
        this.module = module;
        this.intermediate = intermediate;
    }

    private ToHelper()
    {
    }

    public TGeometry toTGeometry()
    {
        return new TGeometryToConverter(module).convert(intermediate);
    }

    public TGeometry toTGeometry(String CRS) throws Exception
    {
        if (!intermediate.getCRS().equalsIgnoreCase(CRS))
            Transform(module).from(intermediate).to(CRS);

        return new TGeometryToConverter(module).convert(intermediate, CRS);
    }

    public GeoJsonObject toGeoJson()
    {
        return new GeoJsonToConverter(module).convert(intermediate);
    }
}
