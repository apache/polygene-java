package org.qi4j.library.spatial.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.geometry.*;
import org.qi4j.api.geometry.internal.Coordinate;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.topo.GeoJSONSwissLakes2013;
import org.qi4j.library.spatial.transformator.GeoJsonTransformator;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jakes on 2/7/14.
 */

/**
 *     http://stackoverflow.com/questions/4619306/list-arrary-double-entering-values
 *     http://worldwind31.arc.nasa.gov/svn/trunk/WorldWind/src/gov/nasa/worldwindx/examples/GeoJSONLoader.java
 *
 *
 */
public class SimpleSpatialRepositoryTest extends AbstractQi4jTest {

    private Visibility visibility = Visibility.module;



    @Rule
    @SuppressWarnings( "PublicField" )
    public TestName testName = new TestName();
    private Logger log;

    @Before
    public void before()
    {
        log = LoggerFactory.getLogger(testName.getMethodName());
        module.injectTo( this );
    }

    @Override
    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        module.services( OrgJsonValueSerializationService.class ).
                visibleIn( visibility ).
                taggedWith(ValueSerialization.Formats.JSON);

        // internal values
        module.values( Coordinate.class, TLinearRing.class, TGeometry.class);

        // API values
        module.values(TPoint.class,TLineString.class, TPolygon.class, TFeature.class);

        module.services(GeometryFactory.class);

    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

    @Service
    GeometryFactory Geometry;

   @Test
   public void foo()  throws Exception {

       UnitOfWork uow = module.newUnitOfWork();
       try
       {


           BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/home/jakes/Projects/QI4J/Spatial/qi4j-sdk/libraries/spatial/src/test/resources/topo/geojson/germany/bavaria.neustadt.geojson"));

          //  BufferedInputStream inputstream = new BufferedInputStream(new FileInputStream("/media/HDD_002/spatial/OpenStreetMap/geofabrik.de/nuenberg.geojson"));

           FeatureCollection featureCollection =
                   new ObjectMapper().readValue(inputstream, FeatureCollection.class);

           System.out.println("Found num of features " + featureCollection.getFeatures().size());

           int count = 0;

           Iterator<Feature> features = featureCollection.getFeatures().iterator();

           while (features.hasNext()) {

               count++;

               Feature feature = features.next();

               TFeature tFeature =  GeoJsonTransformator.withGeometryFactory(Geometry).transformGeoFeature(feature);



           }



       }
       catch( Exception ex )
       {
           ex.printStackTrace();
           // log.error( ex.getMessage(), ex );
           throw ex;
       }
       finally
       {
           uow.discard();
       }


   }

    private Set<TPoint> toPoints(List<LngLatAlt> coordinates)
    {
        // get the shell
        Iterator<LngLatAlt> shellPoints = coordinates.iterator();

        Set<TPoint> points = new HashSet<>();
        while(shellPoints.hasNext())
        {
            LngLatAlt p = shellPoints.next();
            // Geometry.asCoordinate(c.getLatitude(),c.getLongitude(), c.getAltitude() );
            TPoint tpoint = Geometry.asPoint(
                    Geometry.asCoordinate(
                            p.getLatitude(),
                            p.getLongitude(),
                            p.getAltitude()
                    )
            );

            points.add(tpoint);

            // Geometry.asLinearRing(null).
            // Geometry.
        }
        return points;
    }

    private TLinearRing toLinearRing(Set<TPoint> points)
    {
        return Geometry.asLinearRing(points.toArray(new TPoint[0]));
    }




    @Test
    public void testfoo() throws Exception {

        FeatureCollection featureCollection =
                new ObjectMapper().readValue(GeoJSONSwissLakes2013.SWISS_LAKES , FeatureCollection.class);



//        File file = new File("/media/HDD_002/spatial/usa-lines.geojson");
//        FileInputStream fis = new FileInputStream(file);
//
//
//         FeatureCollection featureCollection1 =
//                new ObjectMapper().readValue(fis , FeatureCollection.class);



    }





}
