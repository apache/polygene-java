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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.functional.Specifications;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.MetaInfoDeclaration;
import org.qi4j.bootstrap.PropertyDeclarations;
import org.qi4j.runtime.bootstrap.AssemblyHelper;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.service.ServiceDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.functional.Specifications.and;
import static org.qi4j.functional.Specifications.translate;
import static org.qi4j.functional.Iterables.filter;

/**
 * JAVADOC
 */
public final class ServiceModel
        extends AbstractCompositeModel
        implements ServiceDescriptor
{
    private static Method identityMethod;
    static
    {
        try
        {
            identityMethod = Identity.class.getMethod( "identity" );
        } catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    public static ServiceModel newModel( final Class<? extends ServiceComposite> compositeType,
                                         final Visibility visibility,
                                         final MetaInfo metaInfo,
                                         final List<Class<?>> assemblyConcerns,
                                         final List<Class<?>> sideEffects,
                                         final List<Class<?>> mixins,
                                         final List<Class<?>> roles,
                                         final String moduleName,
                                         final String identity,
                                         final boolean instantiateOnStartup,
                                         AssemblyHelper helper
    )
    {
        PropertyDeclarations propertyDeclarations = new MetaInfoDeclaration();
        ConstraintsModel constraintsModel = new ConstraintsModel( compositeType );
        boolean immutable = metaInfo.get( Immutable.class ) != null;
        PropertiesModel propertiesModel = new PropertiesModel( constraintsModel, propertyDeclarations, immutable );
        StateModel stateModel = new StateModel( propertiesModel );
        MixinsModel mixinsModel = new MixinsModel( compositeType, roles, mixins );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( assemblyConcerns, concerns );
        ConcernsDeclaration.concernDeclarations( compositeType, concerns );
        ConcernsDeclaration concernsModel = new ConcernsDeclaration( concerns );
        SideEffectsDeclaration sideEffectsModel = new SideEffectsDeclaration( compositeType, sideEffects );
        CompositeMethodsModel compositeMethodsModel =
                new CompositeMethodsModel( compositeType, constraintsModel, concernsModel, sideEffectsModel, mixinsModel, helper );

        stateModel.addStateFor( compositeMethodsModel.methods(), mixinsModel );

        return new ServiceModel(
                compositeType, roles, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel, moduleName, identity, instantiateOnStartup );
    }

    private final String identity;
    private final boolean instantiateOnStartup;
    private String moduleName;
    private final Class configurationType;

    public ServiceModel( Class<? extends Composite> compositeType,
                         List<Class<?>> roles,
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
        super( compositeType, roles, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );

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

    public ServiceInstance newInstance( ModuleInstance module )
    {
        Object[] mixins = mixinsModel.newMixinHolder();

        StateHolder stateHolder = stateModel.newBuilderInstance( module );
        stateHolder.getProperty( identityMethod).set( identity );
        stateHolder = stateModel.newInstance( stateHolder );
        ServiceInstance compositeInstance = new ServiceInstance( this, module, mixins, stateHolder );

        try
        {
            // Instantiate all mixins
            ((MixinsModel) mixinsModel).newMixins( compositeInstance,
                    UsesInstance.EMPTY_USES.use( this ),
                    stateHolder,
                    mixins );
        } catch( InvalidCompositeException e )
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
                    new Class[]{type()},
                    serviceInvocationHandler ) );
        } else
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
        Class injectionClass = null;
        Iterable<DependencyModel> configurationThisDependencies = filter( and( translate( new DependencyModel.InjectionTypeFunction(), Specifications.<Class<?>>in( Configuration.class ) ), new DependencyModel.ScopeSpecification( This.class ) ), dependencies() );
        for( DependencyModel dependencyModel : configurationThisDependencies )
        {
            if( dependencyModel.rawInjectionType().equals( Configuration.class ) && dependencyModel.injectionType() instanceof ParameterizedType )
            {
                Class<?> type = Classes.RAW_CLASS.map(((ParameterizedType)dependencyModel.injectionType()).getActualTypeArguments()[0]);
                if( injectionClass == null )
                {
                    injectionClass = type;
                } else
                {
                    if( injectionClass.isAssignableFrom( type ) )
                    {
                        injectionClass = type;
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
