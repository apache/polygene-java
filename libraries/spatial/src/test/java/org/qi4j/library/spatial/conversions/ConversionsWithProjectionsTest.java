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

package org.qi4j.library.spatial.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.FeatureCollection;
import org.junit.Test;
import org.qi4j.api.geometry.TFeatureCollection;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.library.spatial.topo.GeoJSONSwissLakes2013;
import org.qi4j.library.spatial.projection.transformations.TTransformations;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.library.spatial.conversions.TConversions.Convert;


public class ConversionsWithProjectionsTest extends AbstractQi4jTest {

    private final String CRS1 = "EPSG:4326";
    private ObjectMapper geoJsonMapper = new ObjectMapper();


    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        new TGeometryAssembler().assemble(module);
    }

    @Test
    public void whenConvertFromTGeometryToTGeometryConvertProjections() throws Exception {
        TPoint tPoint1 = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry();
        TPoint tPoint2 = (TPoint) Convert(module).from(tPoint1).toTGeometry(CRS1);
        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }


    @Test
    public void whenConvertSwissLakesTranslateProjection() throws Exception
    {
        // convert from CH1903 (EPSG:21781) to EPSG:4326
//        TFeatureCollection swisslakes =  (TFeatureCollection)Convert(module).from(geoJsonMapper.readValue(GeoJSONSwissLakes2013.SWISS_LAKES, FeatureCollection.class))
//         .toTGeometry("EPSG:21781");

        FeatureCollection featureCollection = geoJsonMapper.readValue(GeoJSONSwissLakes2013.SWISS_LAKES, FeatureCollection.class);
        TFeatureCollection tFeatureCollection = (TFeatureCollection)Convert(module).from(featureCollection).toTGeometry();
        tFeatureCollection.setCRS("EPSG:21781");
        System.out.println(tFeatureCollection.getCoordinates().length);
        System.out.println(tFeatureCollection.getNumPoints());

        TTransformations.Transform(module).from(tFeatureCollection).to("EPSG:4326");

        System.out.println("tFeatureCollection " + tFeatureCollection);

        // tFeatureCollection.getCoordinates();


 /**
        TFeatureCollection tFeatureCollection = (TFeatureCollection)Convert(module).from(featureCollection).toTGeometry("EPSG:21781");
        System.out.println(tFeatureCollection);
        TTransformations.Transform(module).from(tFeatureCollection).to("EPSG:4326");
        System.out.println(tFeatureCollection);
  */
    }

}
