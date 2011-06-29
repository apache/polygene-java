package org.qi4j.runtime.injection.provider;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.composite.TransientBuilderInstance;
import org.qi4j.runtime.composite.TransientModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.object.ObjectBuilderInstance;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.ParameterizedType;

/**
 * JAVADOC
 */
public final class UsesInjectionProviderFactory
    implements InjectionProviderFactory
{
    public UsesInjectionProviderFactory()
    {
    }

    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new UsesInjectionProvider( dependencyModel );
    }

    private class UsesInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependency;

        public UsesInjectionProvider( DependencyModel dependency )
        {
            this.dependency = dependency;
        }

        @SuppressWarnings( "unchecked" )
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            UsesInstance uses = context.uses();

            Class injectionType = dependency.rawInjectionType();
            Object usesObject = null;
            if( !Iterable.class.equals( injectionType ) && !ObjectBuilder.class.equals( injectionType ) && !TransientBuilder.class
                .equals( injectionType ) )
            {
                usesObject = uses.useForType( injectionType );
            }

            if( usesObject == null && !dependency.optional())
            {
                Class<?> type;
                if (dependency.injectionType() instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) dependency.injectionType();
                    type = Classes.RAW_CLASS.map( parameterizedType.getActualTypeArguments()[0] );
                } else
                {
                    type = injectionType;
                }

                // No @Uses object provided
                // Try instantiating a Composite or Object for the given type
                ModuleInstance moduleInstance = context.moduleInstance();

                ModelModule<TransientModel> transientModel  = moduleInstance.findTransientModels( type );
                if( transientModel != null )
                {
                    if( Iterable.class.equals( injectionType ) || TransientBuilder.class.equals( injectionType ) )
                    {
                        usesObject = new TransientBuilderInstance( transientModel, uses );
                    }
                    else
                    {
                        StateHolder stateHolder = context.state();
                        transientModel.model().state().checkConstraints( stateHolder );
                        usesObject = transientModel.model()
                            .newCompositeInstance( transientModel.module(), uses, stateHolder );
                    }
                }
                else
                {
                    ModelModule<ObjectModel> objectModel = moduleInstance.findObjectModels( type );
                    if( objectModel != null )
                    {
                        if( Iterable.class.equals( injectionType ) || ObjectBuilder.class.equals( injectionType ) )
                        {
                            usesObject = new ObjectBuilderInstance( context, objectModel.model() );
                        }
                        else
                        {
                            try
                            {
                                usesObject = objectModel.model().newInstance( context );
                            }
                            catch( Exception e )
                            {
                                throw new InjectionProviderException( "Could not instantiate object of class " + type.getName(), e );
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