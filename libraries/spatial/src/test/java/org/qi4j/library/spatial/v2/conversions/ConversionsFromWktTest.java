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
public class ConversionsFromWktTest extends AbstractQi4jTest {

    private ObjectMapper GeoJsonMapper = new ObjectMapper();

    private final String CRS_EPSG_4326 = "EPSG:4326";
    private final String CRS_EPSG_27572 = "EPSG:27572";

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // internal values
        module.values(Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class, TMultiPoint.class, TLineString.class, TPolygon.class, TMultiPolygon.class, TFeature.class, TFeatureCollection.class);

        TGeometry tGeometry = module.forMixin(TGeometry.class).declareDefaults();
        tGeometry.CRS().set(CRS_EPSG_4326);
    }

    @Test
    public void WhenConvertFromWktToTGeometry() throws Exception
    {
        TPoint tPoint = (TPoint)Convert(module).from("POINT(11.57958981111 48.13905780941111 )", CRS_EPSG_27572).toTGeometry();
    }


}
