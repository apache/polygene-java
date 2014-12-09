package org.qi4j.library.spatial.projections;

import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertNotNull;

/**
 * Created by jj on 17.11.14.
 */
public class ProjectionsTransformationTest extends AbstractQi4jTest {

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
    public void testTransform() throws Exception {

        ValueBuilder<TPoint> builder = module.newValueBuilder(TPoint.class);

        // TPoint pointABC = builder.prototype().X(123).Y()

        TPoint pointABC = builder.prototype().of
                (
                        module.newValueBuilder(Coordinate.class).prototype().of(1d),  //x
                        module.newValueBuilder(Coordinate.class).prototype().of(1d)   //y
                );



        // ProjectionsTransformation.transform("", pointABC);

    }
}
