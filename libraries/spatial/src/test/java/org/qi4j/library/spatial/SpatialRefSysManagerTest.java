package org.qi4j.library.spatial;

import org.junit.Test;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.v2.projections.ProjectionsRegistry;
import org.qi4j.library.spatial.v2.projections.SpatialRefSysManager;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.qi4j.api.geometry.TGEOM.TPOINT;

/**
 * Created by jj on 18.11.14.
 */
public class SpatialRefSysManagerTest extends AbstractQi4jTest {

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        // Internal Types
        module.values(
                Coordinate.class,
                TLinearRing.class);

        // API values
        module.values(TPoint.class,TLineString.class, TPolygon.class);
        // module.services(GeometryFactory.class);


        // module.forMixin( SomeType.class ).declareDefaults().someValue().set( "&lt;unknown&gt;" );
    }

    @Test
    public void whenValidSRID() throws Exception
    {
        assertNotNull(SpatialRefSysManager.getCRS("EPSG:4326"));
        System.out.println(SpatialRefSysManager.getCRS("EPSG:4326").toWKT());
    }

    @Test
    public void whenTransformation() throws Exception
    {

        ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);

        // TPoint pointAs_EPSG_4326_1 = builder.prototype().X(10.712809).Y(49.550881);

        // TPoint pointAs_EPSG_4326_1 = module.newValueBuilder(TPoint.class).prototype().X(10.712809).Y(49.550881);

        TPoint pointAs_EPSG_4326_1 = TPOINT(module).x(10.712809).y(49.550881).geometry();

/**
        TPoint point = builder.prototype().of
            (
                    module.newValueBuilder(Coordinate.class).prototype().of(10.712809),  //x
                    module.newValueBuilder(Coordinate.class).prototype().of(49.550881)   //y
            );
*/

        System.out.println(" Array " + Arrays.toString(pointAs_EPSG_4326_1.source()));

       //  SpatialRefSysManager.transform(point, "EPSG:4326");

       TPoint pointAs_EPSG_27572 = (TPoint)SpatialRefSysManager.transform(pointAs_EPSG_4326_1, "EPSG:27572");

        System.out.println(pointAs_EPSG_27572);

        TPoint pointAs_EPSG_4326_2 = (TPoint)SpatialRefSysManager.transform(pointAs_EPSG_27572, "EPSG:4326");

        System.out.println(pointAs_EPSG_4326_2);

        // SpatialRefSysManager.transform(point, "EPSG:26736");

    }

    @Test
    public void dumpRegistries() throws Exception {
        System.out.println(Arrays.toString(new ProjectionsRegistry().dumpRegistries()));

        System.out.println(new ProjectionsRegistry().getSupportedRegistryCodes("epsg"));
    }

}
