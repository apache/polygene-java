package org.qi4j.runtime.service;

import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test of configuration for services
 */
public class ConfigurationTest
    extends AbstractQi4jTest
{
    @Service
    ServiceReference<HelloWorldService> service;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( this.getClass() );
        module.addEntities( HelloWorldConfiguration.class );
        module.addServices( HelloWorldService.class ).identifiedBy( "HelloWorldService" );
        module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
    }

    @Test
    public void whenConfiguredThenSayHelloWorks()
        throws Exception
    {
        objectBuilderFactory.newObjectBuilder( ConfigurationTest.class ).injectTo( this );

        UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<HelloWorldConfiguration> entityBuilder = unit.newEntityBuilder( HelloWorldConfiguration.class, service.identity() );
        HelloWorldConfiguration config = entityBuilder.instance();
        config.phrase().set( "Hey" );
        config.name().set( "Universe" );
        entityBuilder.newInstance();
        unit.complete();

        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hey Universe" ) );
    }

    @Test
    public void whenUnconfiguredThenSayHelloGivesDefaults()
        throws Exception
    {
        objectBuilderFactory.newObjectBuilder( ConfigurationTest.class ).injectTo( this );

        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hello World" ) );
    }

    @Test
    public void givenConfiguredServiceWhenReconfiguredAndRefreshedThenNewConfigurationIsUsed()
        throws Exception
    {
        objectBuilderFactory.newObjectBuilder( ConfigurationTest.class ).injectTo( this );

        HelloWorldConfiguration config;

        {
            UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
            EntityBuilder<HelloWorldConfiguration> entityBuilder = unit.newEntityBuilder( HelloWorldConfiguration.class, service.identity() );
            config = entityBuilder.instance();
            config.phrase().set( "Hello" );
            config.name().set( "World" );
            config = entityBuilder.newInstance();
            unit.complete();
        }

        assertThat( "result is correct", service.get().sayHello(), equalTo( "Hello World" ) );

        {
            UnitOfWork unit = unitOfWorkFactory.newUnitOfWork();
            config = unit.get( config );
            config.phrase().set( "Hey" );
            config.name().set( "Universe" );
            unit.complete();
        }

        assertThat( "new configuration is not used", service.get().sayHello(), equalTo( "Hello World" ) );

        service.get().refresh();

        assertThat( "new configuration is used", service.get().sayHello(), equalTo( "Hey Universe" ) );
    }

    public interface HelloWorld
    {
        String sayHello();
    }

    @Mixins( HelloWorldMixin.class )
    public interface HelloWorldService
        extends HelloWorld, ServiceComposite, Configuration, Activatable
    {
    }

    public interface HelloWorldConfiguration
        extends ConfigurationComposite
    {
        @UseDefaults
        Property<String> phrase();

        @UseDefaults
        Property<String> name();
    }

    public static class HelloWorldMixin
        implements HelloWorld, Activatable
    {
        @This
        Configuration<HelloWorldConfiguration> config;

        public void activate()
            throws Exception
        {
        }

        public void passivate()
            throws Exception
        {
        }

        public String sayHello()
        {
            return config.configuration().phrase() + " " + config.configuration().name();
        }
    }
}