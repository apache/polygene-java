package org.qi4j.library.spatial.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.FeatureCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.spatial.topo.GeoJSONSwissLakes2013;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jakes on 2/7/14.
 */

/**
 *     http://stackoverflow.com/questions/4619306/list-arrary-double-entering-values
 *     http://worldwind31.arc.nasa.gov/svn/trunk/WorldWind/src/gov/nasa/worldwindx/examples/GeoJSONLoader.java
 *
 *
 */
public class SimpleGeoJsonTest extends AbstractQi4jTest {

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

    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

   @Test
   public void foo()  throws Exception {

       UnitOfWork uow = module.newUnitOfWork();
       try
       {

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
