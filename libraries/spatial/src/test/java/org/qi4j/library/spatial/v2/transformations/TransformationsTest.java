package org.qi4j.library.spatial.v2.transformations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.qi4j.api.geometry.TGeometryFactory.TMultiPoint;
import static org.qi4j.api.geometry.TGeometryFactory.TPoint;
import static org.qi4j.api.geometry.TGeometryFactory.TPolygon;
import static org.qi4j.library.spatial.v2.conversions.TConversions.Convert;
import static org.qi4j.library.spatial.v2.transformations.TTransformations.Transform;


/**
 * Created by jj on 04.12.14.
 */
public class TransformationsTest extends AbstractQi4jTest {

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
    public void WhenTransformTPoint() throws Exception
    {
        TPoint tPoint  = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(CRS_EPSG_4326);
        TPoint tPoint1  = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry(CRS_EPSG_4326);

        Transform(module).from(tPoint).to("EPSG:27572");

        System.out.println(tPoint1);
        System.out.println(tPoint);
    }

    @Test
    public void WhenTransformTMultiPoint() throws Exception
    {
        TMultiPoint multiPoint = TMultiPoint(module).points(new double[][]
                {
                        {11.57958981111, 48.13905780941111},
                        {11.57958985111, 48.13905780951111},

                }).geometry(CRS_EPSG_4326);

        Transform(module).from(multiPoint).to("EPSG:27572", 2);
        System.out.println(multiPoint);
    }

    @Test
    public void WhenTransformPolygon() throws Exception
    {
      TPolygon polygon =   TPolygon(module)
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

        Transform(module).from(polygon).to("EPSG:27572", 2);

        System.out.println(polygon);
    }

    @Test
    public void WhenConvertFromTGeometryToTGeometry()
    {
        TPoint tPoint1 = TPoint(module).x(11.57958981111).y(48.13905780941111).geometry();
        TPoint tPoint2 = (TPoint)Convert(module).from(tPoint1).toTGeometry();
        assertTrue(tPoint1.compareTo(tPoint2) == 0);
    }


}
