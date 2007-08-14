/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectInputStream;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilderFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.DependencyResolver;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.model.CompositeContext;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.FragmentModel;
import org.qi4j.api.model.ModifierModel;

/**
 * TODO
 */
public final class CompositeContextImpl<T extends Composite>
    implements CompositeContext<T>
{
    private CompositeModel<T> compositeModel;
    private CompositeBuilderFactoryImpl builderFactory;
    private ConcurrentHashMap<Method, List<InvocationInstance>> invocationInstancePool;
    private CompositeModelFactory modelFactory;


    public CompositeContextImpl( CompositeModel<T> compositeModel, CompositeModelFactory modelFactory, CompositeBuilderFactoryImpl builderFactory )
    {
        this.modelFactory = modelFactory;
        this.compositeModel = compositeModel;
        this.builderFactory = builderFactory;
        invocationInstancePool = new ConcurrentHashMap<Method, List<InvocationInstance>>();
    }

    public CompositeModel<T> getCompositeModel()
    {
        return compositeModel;
    }

    public CompositeModelFactory getCompositeModelFactory()
    {
        return modelFactory;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return builderFactory;
    }

    public InvocationInstance<T> getInvocationInstance( Method method )
    {
        List<InvocationInstance> instances = invocationInstancePool.get( method );

        if( instances == null )
        {
            instances = new ArrayList<InvocationInstance>( 1 );
            invocationInstancePool.put( method, instances );
        }

        synchronized( instances )
        {
            InvocationInstance invocationInstance;
            int size = instances.size();
            if( size > 0 )
            {
                invocationInstance = instances.remove( size - 1 );
            }
            else
            {
                invocationInstance = newInstance( method );
            }

            return invocationInstance;
        }
    }

    public InvocationInstance newInstance( Method method )
    {
        try
        {
            ProxyReferenceInvocationHandler proxyHandler = new ProxyReferenceInvocationHandler();
            Class compositeClass = compositeModel.getCompositeClass();
            ClassLoader classloader = compositeClass.getClassLoader();
            Class[] intfaces = new Class[]{ compositeClass };
            T thisCompositeProxy = (T) Proxy.newProxyInstance( classloader, intfaces, proxyHandler );

            List<ModifierModel> modifierModels = compositeModel.getModifiers( method );
            Object firstModifier = null;
            Object lastModifier = null;
            Field previousModifies = null;
            for( ModifierModel modifierModel : modifierModels )
            {
                Object modifierInstance = newFragment( modifierModel, thisCompositeProxy, method );
                if( firstModifier == null )
                {
                    firstModifier = modifierInstance;
                }
                List<Field> list = modifierModel.getDependencyFields();
                for( Field dependencyField : list )
                {
                    Object dependency = resolveModifierDependency( dependencyField, proxyHandler, method );
                    if( dependency != null )
                    {
                        dependencyField.set( modifierInstance, dependency );
                    }
                }

                // @Modifies
                if( previousModifies != null )
                {
                    if( lastModifier instanceof InvocationHandler )
                    {
                        if( modifierInstance instanceof InvocationHandler )
                        {
                            // IH to another IH
                            previousModifies.set( lastModifier, modifierInstance );
                        }
                        else
                        {
                            // IH to an object modifierModel
                            FragmentInvocationHandler handler = new FragmentInvocationHandler( modifierInstance );
                            previousModifies.set( lastModifier, handler );
                        }
                    }
                    else
                    {
                        if( modifierInstance instanceof InvocationHandler )
                        {
                            // Object modifierModel to IH modifierModel
                            Object modifierProxy = Proxy.newProxyInstance( previousModifies.getType().getClassLoader(), new Class[]{ previousModifies.getType() }, (InvocationHandler) modifierInstance );
                            previousModifies.set( lastModifier, modifierProxy );
                        }
                        else
                        {
                            // Object modifierModel to another object modifierModel
                            previousModifies.set( lastModifier, modifierInstance );
                        }
                    }
                }

                lastModifier = modifierInstance;
                previousModifies = modifierModel.getModifiesField();
            }


            FragmentInvocationHandler mixinInvocationHandler = null;
            if( previousModifies != null )
            {
                mixinInvocationHandler = new FragmentInvocationHandler();
                if( lastModifier instanceof InvocationHandler )
                {
                    previousModifies.set( lastModifier, mixinInvocationHandler );
                }
                else
                {
                    ClassLoader loader = previousModifies.getType().getClassLoader();
                    Object mixinProxy = Proxy.newProxyInstance( loader, new Class[]{ previousModifies.getType() }, mixinInvocationHandler );
                    previousModifies.set( lastModifier, mixinProxy );
                }
            }
            return new InvocationInstance( firstModifier, mixinInvocationHandler, proxyHandler, invocationInstancePool.get( method ) );
        }
        catch( IllegalAccessException e )
        {
            throw new CompositeInstantiationException( "Could not create invocation instance", e );
        }
    }

    private Object resolveModifierDependency( AnnotatedElement annotatedElement, InvocationContext invocationContext, Method method )
    {
        // @Dependency
        Class elementType = getElementType( annotatedElement );
        Class<? extends Object> fragmentClass = compositeModel.getMixin( method.getDeclaringClass() ).getFragmentClass();
        if( elementType.equals( InvocationContext.class ) )
        {
            return invocationContext;
        }
        else if( elementType.equals( AnnotatedElement.class ) )
        {
            Method dependencyMethod;
            if( InvocationHandler.class.isAssignableFrom( fragmentClass ) )
            {
                dependencyMethod = method;
            }
            else
            {
                try
                {
                    dependencyMethod = fragmentClass.getMethod( method.getName(), method.getParameterTypes() );
                }
                catch( NoSuchMethodException e )
                {
                    throw new CompositeInstantiationException( "Could not resolve @Dependency to method in modifier " + fragmentClass.getName() + " for composite " + compositeModel.getCompositeClass().getName(), e );
                }
            }
            return dependencyMethod;
        }
        else
        {
            String dependentName = getDependencyName( annotatedElement.getAnnotation( Dependency.class ), elementType.getName() );
            return resolveFragmentDependency( annotatedElement, dependentName, fragmentClass.getName() );
        }
    }

    private Object resolveFragmentDependency( AnnotatedElement annotatedElement, String dependentName, String location )
        throws CompositeInstantiationException
    {
        Object currentDependency = null;
        for( DependencyResolver resolver : builderFactory.getDependencyResolvers() )
        {
            Object dependency = resolver.resolveDependency( annotatedElement, this );
            if( dependency != null )
            {
                if( currentDependency != null )
                {
                    throw new CompositeInstantiationException( "Dependency " + dependentName + " in fragment " + location + " for composite " + compositeModel.getCompositeClass() + " has ambiguous resolutions." );
                }
                currentDependency = dependency;
            }
        }

        if( currentDependency == null )
        {
            // No object found, check if it's optional
        }
        else
        {
            return currentDependency;
        }
        return null;
    }

    <K> K newFragment( FragmentModel<K> aFragmentModel, T proxy, Method method )
    {
        K instance;
        try
        {
            Class<K> fragmentClass = aFragmentModel.getFragmentClass();
            //noinspection unchecked
            Constructor<K>[] constructors = fragmentClass.getConstructors();
            Constructor<K> useConstructor = choose( constructors, aFragmentModel instanceof ModifierModel );
            if( useConstructor == null )
            {
                throw new CompositeInstantiationException( "No usable constructor in " + fragmentClass.getName() );
            }
            Object[] parameters = getConstructorParameters( useConstructor, proxy, method );
            instance = useConstructor.newInstance( parameters );
        }
        catch( Exception e )
        {
            throw new CompositeInstantiationException( "Could not instantiate class " + aFragmentModel.getFragmentClass().getName(), e );
        }

        resolveUsesFields( aFragmentModel, proxy, instance );

//        List<Field> dependencyFields = aFragmentModel.getDependencyFields();
//        for( Field dependencyField : dependencyFields )
//        {
//            Object dependency = resolveFragmentDependency( dependencyField, dependencyField.getName(), dependencyField.getDeclaringClass().getName() );
//            if( dependency == null )
//            {
//                if( !dependencyField.getAnnotation( Dependency.class ).optional() )
//                {
//                    throw new CompositeInstantiationException( "Dependency '" + dependencyField.getName() + "' in fragment " + dependencyField.getDeclaringClass().getName() + " for composite " + compositeModel.getCompositeClass() + " could not be resolved." );
//                }
//            }
//            else
//            {
//                try
//                {
//                    dependencyField.set( instance, dependency );
//                }
//                catch( IllegalAccessException e )
//                {
//                    throw new CompositeInstantiationException( "Dependency " + dependencyField.getName() + " in fragment " + dependencyField.getDeclaringClass().getName() + " for composite " + compositeModel.getCompositeClass() + " could not be set.", e );
//                }
//            }
//        }
        return instance;
    }

    private Object[] getConstructorParameters( Constructor<? extends Object> useConstructor, T proxy, Method method )
    {
        Class<? extends Object>[] parameterTypes = useConstructor.getParameterTypes();
        Annotation[][] annotations = useConstructor.getParameterAnnotations();
        Object[] parameters = new Object[parameterTypes.length];
        for( int i = 0; i < parameterTypes.length; i++ )
        {
            Class<? extends Object> parameterType = parameterTypes[ i ];

            Dependency depAnnotation = getDependencyAnnotation( annotations[ i ] );
            String dependentName = getDependencyName( depAnnotation, parameterType.getName() );
            Object dependency = null;
            if( method != null ) // In Modifier creation.
            {
                dependency = resolveModifierDependency( parameterType, (InvocationContext) Proxy.getInvocationHandler( proxy ), method );
            }
            if( dependency == null )
            {
                dependency = resolveFragmentDependency( parameterType, dependentName, useConstructor.getName() );
            }
            if( dependency == null && !depAnnotation.optional() )
            {
                throw new CompositeInstantiationException( "Dependency " + parameterType.getName() + " in fragment " + useConstructor.getName() + " for composite " + compositeModel.getCompositeClass() + " could not be resolved." );
            }
            parameters[ i ] = dependency;
        }
        return parameters;
    }

    private <K> Constructor<K> choose( Constructor<K>[] constructors, boolean isModifier )
    {
        SortedSet<Constructor<K>> available = new TreeSet<Constructor<K>>( new SizeComparator() );
        for( Constructor<K> constructor : constructors )
        {
            if( canUse( constructor, isModifier ) )
            {
                available.add( constructor );
            }
        }
        if( available.size() > 0 )
        {
            return available.last();
        }
        return null;
    }

    private boolean canUse( Constructor<? extends Object> constructor, boolean isModifier )
    {
        Class<? extends Object>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();
        for( int i = 0; i < parameterTypes.length; i++ )
        {
            Class<? extends Object> parameterType = parameterTypes[ i ];

            Dependency depAnnotation = getDependencyAnnotation( annotations[ i ] );
            if( depAnnotation != null )
            {
                String dependentName = getDependencyName( depAnnotation, parameterType.getName() );
                if( isModifier )
                {
                    if( canResolveModifierDependency( parameterType ) )
                    {
                        continue;
                    }
                }
                Object dependency = resolveFragmentDependency( parameterType, dependentName, constructor.getName() );
                if( dependency != null || depAnnotation.optional() )
                {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    private boolean canResolveModifierDependency( Class<? extends Object> parameterType )
    {
        return parameterType.equals( InvocationContext.class ) ||
               parameterType.equals( AnnotatedElement.class );
    }

    private Dependency getDependencyAnnotation( Annotation[] annotations )
    {
        for( Annotation annotation : annotations )
        {
            if( annotation instanceof Dependency )
            {
                return (Dependency) annotation;
            }
        }
        return null;
    }

    private String getDependencyName( Dependency annotation, String defaultName )
    {
        String dependentName;
        String value = annotation.value();
        if( value.length() > 0 )
        {
            dependentName = value;
        }
        else
        {
            dependentName = defaultName;
        }
        return dependentName;
    }

    protected <K> void resolveUsesFields( FragmentModel<K> fragmentModel, Object proxy, Object fragment )
    {
        for( Field usesField : fragmentModel.getUsesFields() )
        {
            Tristate tristate = canResolveUses( usesField );
            if( tristate == Tristate.TRUE )
            {
                try
                {
                    usesField.set( fragment, proxy );
                }
                catch( IllegalArgumentException e )
                {
                    throw new CompositeInstantiationException( "The @Uses field " + usesField.getName() + " in fragment " + fragmentModel.getFragmentClass().getName() + " is of type " + usesField.getType().getName() + " which is not type compatible with " + compositeModel.getCompositeClass().getName(), e );
                }
                catch( IllegalAccessException e )
                {
                    throw new CompositeInstantiationException( "The @Uses field " + usesField.getName() + " in fragment " + fragmentModel.getFragmentClass().getName() + " is not accessible.", e );
                }
            }
            if( tristate == Tristate.FALSE )
            {
                throw new CompositeInstantiationException( "Could not resolve @Uses field in fragment " + fragmentModel.getFragmentClass().getName() + " for composite " + compositeModel.getCompositeClass().getName() );
            }
        }
    }

    private Tristate canResolveUses( AnnotatedElement annotatedElement )
    {
        if( compositeModel.isAssignableFrom( getElementType( annotatedElement ) ) )
        {
            return Tristate.TRUE;
        }
        if( annotatedElement.getAnnotation( Uses.class ).optional() )
        {
            return Tristate.OPTIONAL;
        }
        return Tristate.FALSE;
    }

    private Class getElementType( AnnotatedElement annotatedElement )
    {
        Class clazz;
        if( annotatedElement instanceof Class )
        {
            clazz = (Class) annotatedElement;
        }
        else
        {
            clazz = ( (Field) annotatedElement ).getType();
        }
        return clazz;
    }

    private class SizeComparator
        implements Comparator<Constructor>
    {
        public int compare( Constructor cons1, Constructor cons2 )
        {
            return cons1.getParameterTypes().length - cons2.getParameterTypes().length;
        }
    }
}
