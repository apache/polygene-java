package org.qi4j.library.spatial.v2.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;
import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGEOM.TMULTIPOINT;
import static org.qi4j.api.geometry.TGEOM.TPOINT;
import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;


/**
 * Created by jj on 04.12.14.
 */
public class ConversionsWithProjectionsTest extends AbstractQi4jTest {

    private ObjectMapper GeoJsonMapper = new ObjectMapper();

    // private final String CRS1 = "urn:ogc:def:crs:OGC:1.3:CRS84";

    private final String CRS1 ="EPSG:4326";



    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS1);

    }

    @Test
    public void WhenConvertFromTGeometryToTGeometryConvertProjections() throws Exception
    {
        TPoint tPoint1 = TPOINT(module).x(11.57958981111).y(48.13905780941111).geometry();
        TPoint tPoint2 = (TPoint)Convert(module).from(tPoint1).toTGeometry(CRS1);
        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }

}
