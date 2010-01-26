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

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.InvalidMixinException;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.mixin.MixinDescriptor;

/**
 * JAVADOC
 */
public final class MixinModel
    implements Binder, MixinDescriptor, Serializable
{
    private final static Map<Class, Class> enhancedClasses = Collections.synchronizedMap( new HashMap<Class, Class>() );

    private final Class mixinClass;
    private final Class instantiationClass;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;
    private final ConcernsDeclaration concernsDeclaration;
    private final SideEffectsDeclaration sideEffectsDeclaration;
    private final Set<Class> thisMixinTypes;
    public MethodInterceptor mixinInvoker;

    public MixinModel( Class declaredMixinClass )
    {
        injectedFieldsModel = new InjectedFieldsModel( declaredMixinClass );
        injectedMethodsModel = new InjectedMethodsModel( declaredMixinClass );

        this.mixinClass = declaredMixinClass;
        this.instantiationClass = instantiationClass( declaredMixinClass );
        constructorsModel = new ConstructorsModel( instantiationClass );

        List<ConcernDeclaration> concerns = new ArrayList<ConcernDeclaration>();
        ConcernsDeclaration.concernDeclarations( declaredMixinClass, concerns );
        concernsDeclaration = new ConcernsDeclaration( concerns );
        sideEffectsDeclaration = new SideEffectsDeclaration( declaredMixinClass, Collections.<Class<?>>emptyList() );

        thisMixinTypes = buildThisMixinTypes();

        mixinInvoker = new MethodInterceptor()
        {
            public Object intercept( Object obj, Method method, Object[] args, MethodProxy proxy )
                throws Throwable
            {
                return proxy.invokeSuper( obj, args );
            }
        };
    }

    public Class mixinClass()
    {
        return mixinClass;
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( mixinClass );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        constructorsModel.visitModel( modelVisitor );
        injectedFieldsModel.visitModel( modelVisitor );
        injectedMethodsModel.visitModel( modelVisitor );
    }

    // Binding

    public void bind( Resolution context )
        throws BindingException
    {
        constructorsModel.bind( context );
        injectedFieldsModel.bind( context );
        injectedMethodsModel.bind( context );
    }

    // Context

    public Object newInstance( CompositeInstance compositeInstance, StateHolder state, UsesInstance uses )
    {
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        return newInstance( injectionContext );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        Object mixin;
        CompositeInstance compositeInstance = injectionContext.compositeInstance();
        try
        {
            if( Factory.class.isAssignableFrom( instantiationClass ) )
            {
                Enhancer.registerCallbacks( instantiationClass,
                                            new Callback[]{
                                                new ThisCompositeInvoker( compositeInstance ),
                                                NoOp.INSTANCE
                                            } );
            }
            mixin = constructorsModel.newInstance( injectionContext );
        }
        catch( InvalidCompositeException e )
        {
            e.setMixinClass( mixinClass );
            throw e;
        }
        injectedFieldsModel.inject( injectionContext, mixin );
        injectedMethodsModel.inject( injectionContext, mixin );
        if( mixin instanceof Initializable )
        {
            try
            {
                Callback callback = ( (Factory) mixin ).getCallback( 0 );
                ( (Factory) mixin ).setCallback( 0, mixinInvoker );
                try
                {
                    ( (Initializable) mixin ).initialize();
                }
                finally
                {
                    ( (Factory) mixin ).setCallback( 0, callback );
                }
            }
            catch( InitializationException e )
            {
                Class<? extends Composite> compositeType = compositeInstance.type();
                String message = "Unable to initialize " + mixinClass + " in composite " + compositeType;
                throw new ConstructionException( message, e );
            }
        }
        return mixin;
    }

    public Set<Class> thisMixinTypes()
    {
        return thisMixinTypes;
    }

    private Set<Class> buildThisMixinTypes()
    {
        final Set<Class> thisDependencies = new HashSet<Class>();
        visitModel(
            new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
            {
                public void visitDependency( DependencyModel dependencyModel )
                {
                    thisDependencies.add( dependencyModel.rawInjectionType() );
                }
            }
        );
        if( thisDependencies.isEmpty() )
        {
            return Collections.emptySet();
        }
        else
        {
            return thisDependencies;
        }
    }

    protected FragmentInvocationHandler newInvocationHandler( Method method )
    {
        if( InvocationHandler.class.isAssignableFrom( mixinClass )
            && !method.getDeclaringClass().isAssignableFrom( mixinClass ) )
        {
            return new GenericFragmentInvocationHandler();
        }
        else
        {
            if( Factory.class.isAssignableFrom( constructorsModel.getFragmentClass() ) )
            {
                Signature sig = ReflectUtils.getSignature( method );
                MethodProxy methodProxy = MethodProxy.find( constructorsModel.getFragmentClass(), sig );
                if( methodProxy == null )
                {
                    try
                    {
                        Method implMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                        sig = ReflectUtils.getSignature( implMethod );
                        methodProxy = MethodProxy.find( constructorsModel.getFragmentClass(), sig );
                        if( methodProxy == null )
                        {
                            throw new InvalidMixinException( mixinClass, method );
                        }
                    }
                    catch( NoSuchMethodException e )
                    {
                        throw new InvalidMixinException( mixinClass, method );
                    }
                }
                return new TypedMixinInvocationHandler( methodProxy );
            }
            else
            {
                return new TypedModifierInvocationHandler( null );
            }
        }
    }

    public MethodConcernsModel concernsFor( Method method, Class<? extends Composite> type )
    {
        return concernsDeclaration.concernsFor( method, type );
    }

    public MethodSideEffectsModel sideEffectsFor( Method method, Class<? extends Composite> type )
    {
        return sideEffectsDeclaration.sideEffectsFor( method, type );
    }

    @Override
    public String toString()
    {
        return mixinClass.getName();
    }

    public void addThisInjections( final Set<Class> thisDependencies )
    {
        // Add all @This injections
        visitModel(
            new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
            {
                public void visitDependency( DependencyModel dependencyModel )
                {
                    thisDependencies.add( dependencyModel.rawInjectionType() );
                }
            }
        );

        // Add all implemented interfaces
        if( Factory.class.isAssignableFrom( instantiationClass ) )
        {
            Set<Class> classes = Classes.interfacesOf( mixinClass );
            classes.remove( Activatable.class );
            classes.remove( Initializable.class );
            classes.remove( Lifecycle.class );
            thisDependencies.addAll( classes );
        }
    }

    public void activate( Object mixin )
        throws Exception
    {
        if( mixin instanceof Activatable )
        {
            Callback callback = ( (Factory) mixin ).getCallback( 0 );
            ( (Factory) mixin ).setCallback( 0, mixinInvoker );
            try
            {
                ( (Activatable) mixin ).activate();
            }
            finally
            {
                ( (Factory) mixin ).setCallback( 0, callback );
            }
        }
    }

    public void passivate( Object mixin )
        throws Exception
    {
        if( mixin instanceof Activatable )
        {
            Callback callback = ( (Factory) mixin ).getCallback( 0 );
            ( (Factory) mixin ).setCallback( 0, mixinInvoker );
            try
            {
                ( (Activatable) mixin ).passivate();
            }
            finally
            {
                ( (Factory) mixin ).setCallback( 0, callback );
            }
        }
    }

    private Class instantiationClass( Class fragmentClass )
    {
        Class instantiationClass = fragmentClass;
        if( !InvocationHandler.class.isAssignableFrom( fragmentClass ) )
        {
            instantiationClass = enhancedClasses.get( fragmentClass );
            if( instantiationClass == null )
            {
                Enhancer enhancer = createEnhancer( fragmentClass );
                instantiationClass = enhancer.createClass();
                enhancedClasses.put( fragmentClass, instantiationClass );
            }
        }
        return instantiationClass;
    }

    private Enhancer createEnhancer( final Class fragmentClass )
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( fragmentClass );
        // TODO: make this configurable?
        enhancer.setClassLoader( new BridgeClassLoader( fragmentClass.getClassLoader() ) );
        enhancer.setCallbackTypes( new Class[]{ ThisCompositeInvoker.class, NoOp.class } );
        enhancer.setCallbackFilter( new CallbackFilter()
        {
            public int accept( Method method )
            {
                if( Lifecycle.class.isAssignableFrom( method.getDeclaringClass() )
                    && ( "create".equals( method.getName() )
                         || "remove".equals( method.getName() )
                )
                    && method.getParameterTypes().length == 0
                    )
                {
                    return 1; // Lifecycle methods must not be proxied.
                }
                if( method.isSynthetic() )
                {
                    return 1;
                }
                if( !Modifier.isPublic( method.getModifiers() ) )
                {
                    return 1; // Only proxy publich methods
                }
                else if( Modifier.isFinal( method.getModifiers() ) )
                {
                    return 1; // Skip final methods
                }

                if( injectedMethodsModel.isInjected( method ) )
                {
                    return 1;
                }

                for( Class aClass : Classes.interfacesOf( fragmentClass ) )
                {
                    try
                    {
                        aClass.getMethod( method.getName(), method.getParameterTypes() );
                        return 0; // This method comes from an interface - try invoking the proxy
                    }
                    catch( NoSuchMethodException e )
                    {
                    }
                }

                return 1;
            }
        } );
        return enhancer;
    }
}
