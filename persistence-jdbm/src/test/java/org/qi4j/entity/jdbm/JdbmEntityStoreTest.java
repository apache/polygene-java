package org.qi4j.entity.jdbm;

import java.io.File;
import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class JdbmEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( JdbmEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
        module.addComposites( TestComposite.class );
    }

    @Override @After public void tearDown() throws Exception
    {
        super.tearDown();

        boolean deleted = new File( "qi4j.data.db" ).delete();
        deleted = deleted | new File( "qi4j.data.lg" ).delete();
        if( !deleted )
        {
            throw new Exception( "Could not delete test data" );
        }
    }

    @Test
    public void whenNewEntityThenFindEntity()
        throws Exception
    {
        String id = createEntity( null );
        UnitOfWork unitOfWork;
        TestComposite instance;

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        instance = unitOfWork.find( id, TestComposite.class );
        assertThat( "property has correct value", instance.name().get(), equalTo( "Rickard" ) );
        unitOfWork.discard();
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        String id = createEntity( null );

        // Remove entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestComposite instance = unitOfWork.find( id, TestComposite.class );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( id, TestComposite.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    @Test
    public void whenNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        int nrOfEntities = 10000;
        for( int i = 0; i < nrOfEntities; i++ )
        {
            createEntity( null );
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( end - start );
        System.out.println( nrOfEntities / ( time / 1000.0D ) );
    }

    @Test
    public void whenBulkNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        int nrOfEntities = 10000;
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();

        long start = System.currentTimeMillis();

        for( int i = 0; i < nrOfEntities; i++ )
        {
            // Create entity
            CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( TestComposite.class );
            builder.propertiesOfComposite().name().set( "Rickard" );
            TestComposite instance = builder.newInstance();
        }

        unitOfWork.complete();
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( end - start );
        System.out.println( nrOfEntities / ( time / 1000.0D ) );
    }

    @Test
    public void whenFindEntityThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        String id = createEntity( null );

        int nrOfLookups = 10000;
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        for( int i = 0; i < nrOfLookups; i++ )
        {
            TestComposite instance = unitOfWork.find( id, TestComposite.class );
            unitOfWork.clear();
        }
        unitOfWork.discard();

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( time );
        System.out.println( nrOfLookups / ( time / 1000.0D ) );
    }

    private String createEntity( String id )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        CompositeBuilder<TestComposite> builder = unitOfWork.newEntityBuilder( id, TestComposite.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        TestComposite instance = builder.newInstance();
        id = instance.identity().get();
        unitOfWork.complete();
        return id;
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface TestComposite
        extends EntityComposite
    {
        Property<String> name();
    }
}