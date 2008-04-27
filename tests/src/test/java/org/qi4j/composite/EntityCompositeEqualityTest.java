package org.qi4j.composite;

import static org.hamcrest.CoreMatchers.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.structure.Visibility;
import org.qi4j.test.AbstractQi4jTest;

public class EntityCompositeEqualityTest extends AbstractQi4jTest
{
    private UnitOfWork unitOfWork;
    private CompositeBuilder<MyComposite> myCompositeBuilder;

    @Before @Override public void setUp() throws Exception
    {
        super.setUp();
        unitOfWork = this.unitOfWorkFactory.newUnitOfWork();
        myCompositeBuilder = unitOfWork.newEntityBuilder( MyComposite.class );
    }

    @After public void completeUnitOfWork() throws Exception
    {
        unitOfWork.complete();
        super.tearDown();
    }

    @Test
    public void shouldNotBeEqualToNull() throws UnitOfWorkCompletionException
    {
        MyComposite simpleComposite = myCompositeBuilder.newInstance();
        assertThat( "simpleComposite is not equal to null", simpleComposite.equals( null ), equalTo( false ) );
    }

    @Test
    public void shouldBeEqualToItself()
    {
        MyComposite simpleComposite = myCompositeBuilder.newInstance();
        assertThat( "simple composite is equal to itself", simpleComposite.equals( simpleComposite ), equalTo( true ) );
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class, MemoryEntityStoreService.class ).visibleIn( Visibility.layer );
        module.addObjects( EntityCompositeEqualityTest.class );
        module.addComposites( MyComposite.class );
    }

    private static interface MyComposite extends EntityComposite
    {
        Property<String> name();
    }
}
