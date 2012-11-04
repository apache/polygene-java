package org.qi4j.runtime.injection.provider;

import java.lang.reflect.Constructor;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private static class UsesInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = uses.useForType( injectionType );

            if( usesObject == null && !dependency.optional() )
            {
                // No @Uses object provided
                // Try instantiating a Transient or Object for the given type
                ModuleInstance moduleInstance = context.module();

                try
                {
                    if( context.instance() != null )
                    {
                        uses = uses.use( context.instance() );
                    }
                    usesObject = moduleInstance.newTransient( injectionType, uses.toArray() );
                }
                catch( NoSuchTransientException e )
                {
                    try
                    {
                        usesObject = moduleInstance.newObject( injectionType, uses.toArray() );
                    }
                    catch( NoSuchObjectException e1 )
                    {
                        // Could not instantiate an instance - to try instantiate as plain class
                        try
                        {
                            usesObject = injectionType.newInstance();
                        }
                        catch( Throwable e2 )
                        {
                            // Could not instantiate - try with this as first argument
                            try
                            {
                                Constructor constructor = injectionType.getDeclaredConstructor( context.instance()
                                                                                                    .getClass() );
                                if( !constructor.isAccessible() )
                                {
                                    constructor.setAccessible( true );
                                }
                                usesObject = constructor.newInstance( context.instance() );
                            }
                            catch( Throwable e3 )
                            {
                                // Really can't instantiate it - ignore
                            }
                        }
                    }
                }

                if( usesObject != null )
                {
                    context.setUses( context.uses().use( usesObject ) ); // Use this for other injections in same graph
                }

                return usesObject;
            }
            else
            {
                return usesObject;
            }
        }
    }
}