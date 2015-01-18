package org.qi4j.library.spatial.transformations;

import org.junit.Test;
import org.qi4j.api.geometry.TMultiPoint;
import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.geometry.TPolygon;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.library.spatial.formats.conversions.TConversions;
import org.qi4j.library.spatial.projections.transformations.TTransformations;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.*;


public class TransformationsTest extends AbstractQi4jTest
{

    private final static String CRS_EPSG_4326 = "EPSG:4326";
    private final static String CRS_EPSG_27572 = "EPSG:27572";

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException
    {
        new TGeometryAssembler().assemble(module);
    }

    @Test
    public void whenTransformTPoint() throws Exception
    {
        TPoint tPoint = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(CRS_EPSG_4326);
        TTransformations.Transform(module).from(tPoint).to("EPSG:27572");
    }

    @Test
    public void whenTransformTMultiPoint() throws Exception
    {
        TMultiPoint multiPoint = TMultiPoint(module).points(new double[][]
                {
                        {11.57958981111, 48.13905780941111},
                        {11.57958985111, 48.13905780951111},

                }).geometry(CRS_EPSG_4326);

        TTransformations.Transform(module).from(multiPoint).to("EPSG:27572", 2);
    }

    @Test
    public void whenTransformPolygon() throws Exception
    {
        TPolygon polygon = TPolygon(module)
                .shell
                        (
                                new double[][]
                                        {
                                                {11.32965087890625, 48.122101028190805},
                                                {11.394195556640625, 48.28593438872724},
                                                {11.9366455078125, 48.232906106325146},
                                                {11.852874755859375, 47.95038564051011},
                                                {11.36810302734375, 47.94486657921015},
                                                {11.32965087890625, 48.122101028190805}
                                        }
                        ).geometry(CRS_EPSG_4326);

        TTransformations.Transform(module).from(polygon).to("EPSG:27572", 2);
    }

    @Test
    public void whenConvertFromTGeometryToTGeometry()
    {
        TPoint tPoint1 = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry();
        TPoint tPoint2 = (TPoint) TConversions.Convert(module).from(tPoint1).toTGeometry();
        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }


}
