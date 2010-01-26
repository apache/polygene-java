/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.service;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.MetaInfoDeclaration;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.composite.AbstractCompositeModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernDeclaration;
import org.qi4j.runtime.composite.ConcernsDeclaration;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.SideEffectsDeclaration;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.service.ServiceDescriptor;

/**
 * JAVADOC
 */
public final class ServiceModel
    extends AbstractCompositeModel
    implements ServiceDescriptor, Serializable
{
    public static ServiceModel newModel( final Class<? extends ServiceComposite> compositeType,
                                         final Visibility visibility,
                                         final MetaInfo metaInfo,
                                         final List<Class<?>> assemblyConcerns,
                                         final List<Class<?>> sideEffects,
                                         final List<Class<?>> mixins,
                                         final String moduleName,
                                         final String identity,
                                         final boolean instantiateOnStartup
    )
    {
        PropertyDeclarations propertyDeclarations = new MetaInfoDeclaration();
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        PropertiesModel propertiesModel = new PropertiesModel( constraintsModel, propertyDeclarations, immutable );
        StateModel stateModel = new StateModel( propertiesModel );
        MixinsModel mixinsModel = new MixinsModel( compositeType, mixins );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( assemblyConcerns, concerns );
        ConcernsDeclaration.concernDeclarations( compositeType, concerns );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( concerns );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType, sideEffects );
        CompositeMethodsModel compositeMethodsModel =
            new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel );
        stateModel.addStateFor( compositeMethodsModel.methods(), compositeType );

        return new ServiceModel(
            compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel, moduleName, identity, instantiateOnStartup );
    }

    private final String identity;
    private final boolean instantiateOnStartup;
    private String moduleName;
    private final Class configurationType;

    public ServiceModel( Class<? extends Composite> compositeType,
                         Visibility visibility,
                         MetaInfo metaInfo,
                         MixinsModel mixinsModel,
                         StateModel stateModel,
                         CompositeMethodsModel compositeMethodsModel,
                         String moduleName,
                         String identity,
                         boolean instantiateOnStartup
    )
    {
        super( compositeType, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

        this.identity = identity;
        this.instantiateOnStartup = instantiateOnStartup;
        this.moduleName = moduleName;

        // Calculate configuration type
        this.configurationType = calculateConfigurationType();
    }

    public boolean isInstantiateOnStartup()
    {
        return instantiateOnStartup;
    }

    public String identity()
    {
        return identity;
    }

    public String moduleName()
    {
        return moduleName;
    }

    public <T> Class<T> configurationType()
    {
        return configurationType;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        compositeMethodsModel.visitModel( modelVisitor );
        mixinsModel.visitModel( modelVisitor );
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), this, null, null );
        compositeMethodsModel.bind( resolution );
        mixinsModel.bind( resolution );
    }

    public boolean isServiceFor( Type serviceType, Visibility visibility )
    {
        // Check visibility
        if( visibility != visibility() )
        {
            return false;
        }

        // Check types
        if( serviceType instanceof Class )
        {
            // Plain class check
            Class serviceClass = (Class) serviceType;
            return serviceClass.isAssignableFrom( type() );
        }
        else if( serviceType instanceof ParameterizedType )
        {
            // Parameterized type check. This is useful for example Wrapper<Foo> usages
            ParameterizedType paramType = (ParameterizedType) serviceType;
            Class rawClass = (Class) paramType.getRawType();
            Set<Type> types = Classes.genericInterfacesOf( type() );
            for( Type type1 : types )
            {
                if( type1 instanceof ParameterizedType && rawClass.isAssignableFrom( Classes.getRawClass( type1 ) ) )
                {
                    // Check params
                    Type[] actualTypes = paramType.getActualTypeArguments();
                    Type[] actualServiceTypes = ( (ParameterizedType) type1 ).getActualTypeArguments();
                    for( int i = 0; i < actualTypes.length; i++ )
                    {
                        Type actualType = actualTypes[ i ];
                        if( actualType instanceof Class )
                        {
                            Class actualClass = (Class) actualType;
                            Class actualServiceType = (Class) actualServiceTypes[ i ];
                            if( !actualClass.isAssignableFrom( actualServiceType ) )
                            {
                                return false;
                            }
                        }
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public ServiceInstance newInstance( ModuleInstance module )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        // Service has no state
        StateHolder stateHolder = new StateHolder()
        {
            public <T> Property<T> getProperty( final Method propertyMethod )
            {
                if( QualifiedName.fromMethod( propertyMethod )
                    .equals( QualifiedName.fromClass( Identity.class, "identity" ) ) )
                {
                    PropertyModel propertyDescriptor = (PropertyModel) stateModel.getPropertyByQualifiedName( QualifiedName.fromMethod( propertyMethod ) );
                    return propertyDescriptor.newInstance( identity );
                }
                else
                {
                    // State is set to defaults
                    return new ComputedPropertyInstance<T>( new GenericPropertyInfo( propertyMethod ) )
                    {
                        public T get()
                        {
                            return null;
                        }
                    };
                }
            }

            public <T> Property<T> getProperty( QualifiedName name )
            {
                // Create a dummy one...
                return new ComputedPropertyInstance<T>( new GenericPropertyInfo( null, true, true, name, null ) )
                {
                    public T get()
                    {
                        return null;
                    }
                };
            }

            public void visitProperties( StateVisitor visitor )
            {
            }
        };
        stateHolder = stateModel.newInstance( stateHolder );
        ServiceInstance compositeInstance = new ServiceInstance( this, module, mixins, stateHolder );

        try
        {
            // Instantiate all mixins
            ( (MixinsModel) mixinsModel ).newMixins( compositeInstance,
                                                     UsesInstance.EMPTY_USES.use( this ),
                                                     stateHolder,
                                                     mixins );
        }
        catch( InvalidCompositeException e )
        {
            e.setFailingCompositeType( type() );
            e.setMessage( "Invalid Cyclic Mixin usage dependency" );
            throw e;
        }

        return compositeInstance;
    }

    public Composite newProxy( InvocationHandler serviceInvocationHandler )
    {
        if( type().isInterface() )
        {
            return Composite.class.cast( Proxy.newProxyInstance( type().getClassLoader(),
                                                                 new Class[]{ type() },
                                                                 serviceInvocationHandler ) );
        }
        else
        {
            Class[] interfaces = type().getInterfaces();
            return Composite.class.cast( Proxy.newProxyInstance( type().getClassLoader(),
                                                                 interfaces,
                                                                 serviceInvocationHandler ) );
        }
    }

    @Override
    public String toString()
    {
        return type().getName() + ":" + identity;
    }

    public Class calculateConfigurationType()
    {
        final List<DependencyModel> dependencyModels = new ArrayList<DependencyModel>();
        visitModel( new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
        {
            @Override
            public void visitDependency( DependencyModel dependencyModel )
            {
                dependencyModels.add( dependencyModel );
            }
        } );

        Class injectionClass = null;
        for( DependencyModel dependencyModel : dependencyModels )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) )
            {
                if( injectionClass == null )
                {
                    injectionClass = dependencyModel.injectionClass();
                }
                else
                {
                    if( injectionClass.isAssignableFrom( dependencyModel.injectionClass() ) )
                    {
                        injectionClass = dependencyModel.injectionClass();
                    }
                }
            }
        }
        return injectionClass;
    }

    public void activate( Object[] mixins )
        throws Exception
    {
        mixinsModel.activate( mixins );
    }

    public void passivate( Object[] mixins )
        throws Exception
    {
        mixinsModel.passivate( mixins );
    }
}
