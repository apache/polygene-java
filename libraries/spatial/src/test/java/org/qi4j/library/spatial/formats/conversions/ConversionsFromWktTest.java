package org.qi4j.library.spatial.formats.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.test.AbstractQi4jTest;
import static org.junit.Assert.assertTrue;


public class ConversionsFromWktTest extends AbstractQi4jTest
{

    private final String CRS_EPSG_4326 = "EPSG:4326";
    private final String CRS_EPSG_27572 = "EPSG:27572";
    private ObjectMapper GeoJsonMapper = new ObjectMapper();

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        new TGeometryAssembler().assemble(module);
    }

    @Test
    public void WhenConvertFromWktToTGeometry() throws Exception
    {
        TPoint tPoint = (TPoint) TConversions.Convert(module).from("POINT(11.57958981111 48.13905780941111 )", CRS_EPSG_27572).toTGeometry();
        assertTrue(tPoint != null);
    }
}
