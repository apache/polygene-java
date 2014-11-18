package org.qi4j.library.spatial.projections;

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
public class ProjectionServiceTest extends AbstractQi4jTest {

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
        module.services( OrgJsonValueSerializationService.class).
                visibleIn( visibility ).
                taggedWith(ValueSerialization.Formats.JSON);

        module.services(ProjectionService.class);

    }

    @Service
    @SuppressWarnings( "ProtectedField" )
    protected ValueSerialization valueSerialization;

    @Service
    ProjectionService projectionService;

   @Test
   public void foo()  throws Exception {

       UnitOfWork uow = module.newUnitOfWork();
       try
       {
           projectionService.test();
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
    public void testSupportedCodes()  throws Exception {

        UnitOfWork uow = module.newUnitOfWork();

        try
        {
            System.out.println(projectionService.getSupportedSRID("EPSG")); // "esri")); // "EPSG"));
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


}
