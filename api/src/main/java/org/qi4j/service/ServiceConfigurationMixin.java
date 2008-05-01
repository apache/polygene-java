package org.qi4j.service;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.Composite;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.Entity;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.Lifecycle;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.property.PropertyMapper;
import org.qi4j.property.PropertyMixin;

/**
 * Generic mixin that handles invocations on configuration.
 * <p>
 * Services that store configuration in EntityComposites should
 * declare the following to access it:
 * </p>
 * <pre><code>
 * &#64This MyServiceConfiguration config;
 * </code></pre>
 */
@AppliesTo( { PropertyMixin.PropertyFilter.class, Lifecycle.class, Entity.class, Composite.class } )
public class ServiceConfigurationMixin
    implements InvocationHandler
{
    private @Structure UnitOfWorkFactory uowf;
    private @Uses ServiceDescriptor descriptor;

    private Object configuration;
    private UnitOfWork uow;

    public synchronized Object invoke( Object proxy, Method method, Object[] arguments ) throws Throwable
    {
        if( configuration == null )
        {
            uow = uowf.newUnitOfWork();
            try
            {
                String id = descriptor.identity();
                configuration = uow.find( id, method.getDeclaringClass() );
            }
            catch( EntityCompositeNotFoundException e )
            {
                configuration = uow.newEntityBuilder( descriptor.identity(), method.getDeclaringClass() ).newInstance();

                // Check for defaults
                InputStream asStream = getClass().getResourceAsStream( "/" + descriptor.identity() + ".properties" );
                if( asStream != null )
                {
                    PropertyMapper.map( asStream, (Composite) configuration );
                }
                uow.complete();
                uow = uowf.newUnitOfWork();
                configuration = uow.dereference( configuration );
            }
            uow.pause();
        }

        return method.invoke( configuration, arguments );
    }
}
