package org.qi4j.service;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.ThisCompositeAs;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreComposite;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class ServiceConfigurationTest
    extends AbstractQi4jTest
{
    @Service ServiceReference<HelloWorld> service;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addObjects( this.getClass() );
        module.addComposites( HelloWorldConfiguration.class );
        module.addServices( HelloWorldService.class );
        module.addServices( MemoryEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
    }

    @Test
    public void whenConfiguredThenSayHelloWorks()
        throws Exception
    {
        objectBuilderFactory.newObjectBuilder( ServiceConfigurationTest.class ).injectTo( this );

        UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
        HelloWorldConfiguration config = unit.newEntity( service.identity().get(), HelloWorldConfiguration.class );
        config.phrase().set( "Hello" );
        config.name().set( "World" );
        unit.complete();

        Assert.assertThat( "result is correct", service.get().sayHello(), CoreMatchers.equalTo( "Hello World" ) );
        service.releaseService();
    }

    public interface HelloWorld
    {
        String sayHello();
    }

    @Mixins( HelloWorldMixin.class )
    public interface HelloWorldService
        extends HelloWorld, ServiceComposite, Activatable
    {
    }

    public interface HelloWorldConfiguration
        extends EntityComposite
    {
        Property<String> phrase();

        Property<String> name();
    }

    public static class HelloWorldMixin
        implements HelloWorld, Activatable
    {
        @ThisCompositeAs
        HelloWorldConfiguration config;

        String result;

        public String sayHello()
        {
            return result;
        }

        public void activate() throws Exception
        {
            result = config.phrase() + " " + config.name();
        }

        public void passivate() throws Exception
        {
        }
    }
}
