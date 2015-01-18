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

package org.qi4j.library.spatial.assembly;

import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TCircle;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.Assemblers;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.projections.ProjectionsRegistry;

public class TGeometryAssembler
        extends Assemblers.VisibilityIdentity<TGeometryAssembler>
{
    private static final String CRS_EPSG_4326 = "EPSG:4326";
    private static String DEFAULT_CRS = CRS_EPSG_4326;


    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        // internal values
        module.values(Coordinate.class, TLinearRing.class, TCircle.class, TGeometry.class);
        // API values
        module.values(
                TCRS.class,
                TPoint.class,
                TMultiPoint.class,
                TLineString.class,
                TMultiLineString.class,
                TPolygon.class,
                TMultiPolygon.class,
                TFeature.class,
                TFeatureCollection.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(DEFAULT_CRS);
    }

    public TGeometryAssembler withCRS(String crs) throws AssemblyException
    {
        try
        {
            CoordinateReferenceSystem ref = new ProjectionsRegistry().getReferenceSystem(crs);
            if (ref == null || ref.getCoordinateSystem() == null)
            {
                throw new AssemblyException("Projection CRS " + crs + " invalid.");
            }

        } catch (CRSException _ex)
        {
            throw new AssemblyException("Projection CRS " + crs + " invalid.", _ex);
        }

        DEFAULT_CRS = crs;
        return this;
    }
}
