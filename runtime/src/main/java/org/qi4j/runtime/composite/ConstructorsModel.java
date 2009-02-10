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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.Serializable;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedParametersModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.util.Annotations;

/**
 * TODO
 */
public final class ConstructorsModel
    implements Binder, Serializable
{
    private final Class fragmentClass;
    private final List<ConstructorModel> constructorModels;
    private List<ConstructorModel> boundConstructors;

    public ConstructorsModel( Class fragmentClass )
    {
        this.fragmentClass = instantiationClass( fragmentClass );

        constructorModels = new ArrayList<ConstructorModel>();
        Constructor[] constructors = this.fragmentClass.getDeclaredConstructors();
        for( Constructor constructor : constructors )
        {
            ConstructorModel constructorModel = newConstructorModel( this.fragmentClass, constructor );
            if( constructorModel != null )
            {
                constructorModels.add( constructorModel );
            }
        }
    }

    private ConstructorModel newConstructorModel( Class fragmentClass, Constructor constructor )
    {
        int idx = 0;
        InjectedParametersModel parameters = new InjectedParametersModel();
        Annotation[][] parameterAnnotations = getConstructorAnnotations( fragmentClass, constructor );
        for( Type type : constructor.getGenericParameterTypes() )
        {
            final Annotation injectionAnnotation = Annotations.getInjectionAnnotation( parameterAnnotations[ idx ] );
            if( injectionAnnotation == null )
            {
                return null; // invalid constructor parameter
            }

            boolean optional = DependencyModel.isOptional( injectionAnnotation, parameterAnnotations[idx]);

            DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, type, fragmentClass, optional );
            parameters.addDependency( dependencyModel );
            idx++;
        }
        return new ConstructorModel( constructor, parameters );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        if( boundConstructors != null )
        {
            for( ConstructorModel constructorModel : boundConstructors )
            {
                constructorModel.visitModel( modelVisitor );
            }
        }
        else
        {
            for( ConstructorModel constructorModel : constructorModels )
            {
                constructorModel.visitModel( modelVisitor );
            }
        }
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        boundConstructors = new ArrayList<ConstructorModel>();
        for( ConstructorModel constructorModel : constructorModels )
        {
            try
            {
                constructorModel.bind( resolution );
                boundConstructors.add( constructorModel );
            }
            catch( Exception e )
            {
                // Ignore
                e.printStackTrace();
            }
        }

        if( boundConstructors.size() == 0 )
        {
            StringBuilder messageBuilder = new StringBuilder( "Found no constructor that could be bound: " );
            if( resolution.composite() == null )
            {
                messageBuilder.append( resolution.object().toString() );
            }
            else
            {
                messageBuilder.append( fragmentClass.getName() )
                    .append( " in " )
                    .append( resolution.composite().toString() );
            }

            if( messageBuilder.indexOf( "$" ) >= 0 )
            {
                messageBuilder.append( "\nNon-static inner classes can not be used as Mixin implementations." );
            }

            String message = messageBuilder.toString();
            throw new BindingException( message );
        }

        // Sort based on parameter count
        Collections.sort( boundConstructors, new Comparator<ConstructorModel>()
        {
            public int compare( ConstructorModel o1, ConstructorModel o2 )
            {
                Integer model2ParametersCount = o2.constructor().getParameterTypes().length;
                int model1ParametersCount = o1.constructor().getParameterTypes().length;
                return model2ParametersCount.compareTo( model1ParametersCount );
            }
        } );
    }

    public Object newInstance( InjectionContext injectionContext )
    {
        // Try all bound constructors, in order
        ConstructionException exception = null;
        for( ConstructorModel constructorModel : boundConstructors )
        {
            try
            {
                return constructorModel.newInstance( injectionContext );
            }
            catch( ConstructionException e )
            {
                exception = e;
            }
        }

        throw exception;
    }

    private Annotation[][] getConstructorAnnotations( Class fragmentClass, Constructor constructor )
    {
        Annotation[][] parameterAnnotations;
        if( Factory.class.isAssignableFrom( fragmentClass ) )
        {
            try
            {
                Class[] constructorParameterTypes = constructor.getParameterTypes();
                Class fragmentSuperClass = fragmentClass.getSuperclass();
                Constructor realConstructor = fragmentSuperClass.getDeclaredConstructor( constructorParameterTypes );
                parameterAnnotations = realConstructor.getParameterAnnotations();
            }
            catch( NoSuchMethodException e )
            {
                // Shouldn't happen
                throw new InternalError( "Could not get real constructor of class " + fragmentClass.getName() );
            }
        }
        else
        {
            parameterAnnotations = constructor.getParameterAnnotations();
        }

        return parameterAnnotations;
    }

    private Class instantiationClass( Class fragmentClass )
    {
        Class instantiationClass = fragmentClass;
        if( Modifier.isAbstract( fragmentClass.getModifiers() ) )
        {
            Enhancer enhancer = createEnchancer( fragmentClass );
            instantiationClass = enhancer.createClass();
            Enhancer.registerStaticCallbacks( instantiationClass, new Callback[]{ NoOp.INSTANCE } );
        }
        return instantiationClass;
    }

    private Enhancer createEnchancer( Class fragmentClass )
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass( fragmentClass );
        // TODO: make this configurable?
        enhancer.setClassLoader( new BridgeClassLoader( fragmentClass.getClassLoader() ) );
        enhancer.setCallbackTypes( new Class[]{ NoOp.class } );
        enhancer.setCallbackFilter( new CallbackFilter()
        {
            public int accept( Method method )
            {
                return 0;
            }
        } );
        return enhancer;
    }

    private static class BridgeClassLoader extends ClassLoader
    {
        private static final String CGLIB_PACKAGE_NAME = "net.sf.cglib";
        private static final ClassLoader CGLIB_CLASS_LOADER = Enhancer.class.getClassLoader();

        private BridgeClassLoader( ClassLoader mixinClassLoader )
        {
            super( mixinClassLoader );
        }

        @Override
        protected Class<?> loadClass( String aClassName, boolean isResolve )
            throws ClassNotFoundException
        {
            if( aClassName.startsWith( CGLIB_PACKAGE_NAME ) )
            {
                Class<?> clazz = CGLIB_CLASS_LOADER.loadClass( aClassName );

                if( isResolve )
                {
                    resolveClass( clazz );
                }

                return clazz;
            }

            return super.loadClass( aClassName, isResolve );
        }
    }
}
