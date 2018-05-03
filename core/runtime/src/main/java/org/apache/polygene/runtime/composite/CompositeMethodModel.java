/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.composite;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.common.ConstructionException;
import org.apache.polygene.api.composite.MethodDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.util.HierarchicalVisitor;
import org.apache.polygene.api.util.VisitableHierarchy;
import org.apache.polygene.runtime.injection.Dependencies;
import org.apache.polygene.runtime.injection.DependencyModel;

/**
 * JAVADOC
 */
public final class CompositeMethodModel
    implements MethodDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    // Model
    private final Method method;
    private Method invocationMethod; // This will be the _ prefixed method on typed mixins
    private final ConstraintsModel constraints;
    private final ConcernsModel concerns;
    private final SideEffectsModel sideEffects;
    private final MixinsModel mixins;
    private final AnnotatedElement annotations;

    // Context
//    private final SynchronizedCompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();
    private final ConcurrentLinkedQueue<CompositeMethodInstance> instancePool = new ConcurrentLinkedQueue<>();
    private final ConstraintsInstance constraintsInstance;

    public CompositeMethodModel( Method method,
                                 ConstraintsModel constraintsModel,
                                 ConcernsModel concernsModel,
                                 SideEffectsModel sideEffectsModel,
                                 MixinsModel mixinsModel
    )
    {
        this.method = method;
        mixins = mixinsModel;
        concerns = concernsModel;
        sideEffects = sideEffectsModel;
        constraints = constraintsModel;
        constraintsInstance = constraints.newInstance();
        annotations = new CompositeMethodAnnotatedElement();
//        instancePool = new SynchronizedCompositeMethodInstancePool();
    }

    // Model

    @Override
    public Method method()
    {
        return method;
    }

    public MixinModel mixin()
    {
        return mixins.mixinFor( method );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Stream<DependencyModel> dependencies()
    {
        Stream<? extends Dependencies> concerns = Stream.of( this.concerns, sideEffects );
        Stream<? extends Dependencies> filteredNonNull = concerns.filter( Objects::nonNull );
        return filteredNonNull.flatMap( Dependencies::dependencies );
    }

    // Context
    public Object invoke( Object composite, Object[] params, MixinsInstance mixins, ModuleDescriptor module )
        throws Throwable
    {
        constraintsInstance.checkValid( composite, method, params );

        CompositeMethodInstance methodInstance = getInstance( module );
        try
        {
            return mixins.invoke( composite, params, methodInstance );
        }
        finally
        {
            instancePool.offer( methodInstance );
        }
    }

    private CompositeMethodInstance getInstance( ModuleDescriptor module )
    {
        CompositeMethodInstance methodInstance = instancePool.poll();
        if( methodInstance == null )
        {
            methodInstance = newCompositeMethodInstance( module );
        }

        return methodInstance;
    }

    private CompositeMethodInstance newCompositeMethodInstance( ModuleDescriptor module )
        throws ConstructionException
    {
        FragmentInvocationHandler mixinInvocationHandler = mixins.newInvocationHandler( method );
        InvocationHandler invoker = mixinInvocationHandler;
        if( concerns != ConcernsModel.EMPTY_CONCERNS )
        {
            ConcernsInstance concernsInstance = concerns.newInstance( method, module, mixinInvocationHandler );
            invoker = concernsInstance;
        }
        if( sideEffects != SideEffectsModel.EMPTY_SIDEEFFECTS )
        {
            SideEffectsInstance sideEffectsInstance = sideEffects.newInstance( method, module, invoker );
            invoker = sideEffectsInstance;
        }

        if( invocationMethod == null )
        {
            MixinModel model = mixins.mixinFor( method );
            if( !InvocationHandler.class.isAssignableFrom( model.mixinClass() ) )
            {
                try
                {
                    invocationMethod = model.instantiationClass()
                        .getMethod( "_" + method.getName(), method.getParameterTypes() );
                }
                catch( NoSuchMethodException e )
                {
                    invocationMethod = method;
//                    throw new ConstructionException( "Could not find the subclass method", e );
                }
            }
            else
            {
                invocationMethod = method;
            }
        }

        mixinInvocationHandler.setMethod( invocationMethod );

        return new CompositeMethodInstance( invoker, mixinInvocationHandler, method, mixins.methodIndex.get( method ) );
    }

    public AnnotatedElement annotatedElement()
    {
        return annotations;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            constraints.accept( modelVisitor );
            concerns.accept( modelVisitor );
            sideEffects.accept( modelVisitor );
        }
        return modelVisitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        return method.toGenericString();
    }

    public Iterable<Method> invocationsFor( Class<?> mixinClass )
    {
        return mixins.invocationsFor( mixinClass ).collect( Collectors.toList() );
    }

    public class CompositeMethodAnnotatedElement
        implements AnnotatedElement
    {
        @Override
        public boolean isAnnotationPresent( Class<? extends Annotation> annotationClass )
        {
            // Check method
            if( method.isAnnotationPresent( annotationClass ) )
            {
                return true;
            }

            // Check mixin
            try
            {
                MixinModel model = mixins.mixinFor( method );
                return !GenericPredicate.INSTANCE.test( model.mixinClass() )
                       && ( model.mixinClass().getMethod( method.getName(), method.getParameterTypes() )
                                 .isAnnotationPresent( annotationClass ) );
            }
            catch( NoSuchMethodException e )
            {
                return false;
            }
        }

        @Override
        public <T extends Annotation> T getAnnotation( Class<T> annotationClass )
        {
            // Check mixin
            try
            {
                MixinModel model = mixins.mixinFor( method );
                if( !GenericPredicate.INSTANCE.test( model.mixinClass() ) )
                {
                    T annotation = annotationClass.cast( model.mixinClass()
                                                             .getMethod( method.getName(), method.getParameterTypes() )
                                                             .getAnnotation( annotationClass ) );
                    if( annotation != null )
                    {
                        return annotation;
                    }
                }
            }
            catch( NoSuchMethodException e )
            {
                // Ignore
            }

            // Check method
            return method.getAnnotation( annotationClass );
        }

        @Override
        public Annotation[] getAnnotations()
        {
            // Add mixin annotations
            List<Annotation> annotations = new ArrayList<>();
            MixinModel model = mixins.mixinFor( method );
            Annotation[] mixinAnnotations = new Annotation[ 0 ];
            if( !GenericPredicate.INSTANCE.test( model.mixinClass() ) )
            {
                mixinAnnotations = model.mixinClass().getAnnotations();
                annotations.addAll( Arrays.asList( mixinAnnotations ) );
            }

            // Add method annotations, but don't include duplicates
            Annotation[] methodAnnotations = method.getAnnotations();
            next:
            for( Annotation methodAnnotation : methodAnnotations )
            {
                for( int i = 0; i < mixinAnnotations.length; i++ )
                {
                    if( annotations.get( i ).annotationType().equals( methodAnnotation.annotationType() ) )
                    {
                        continue next;
                    }
                }

                annotations.add( methodAnnotation );
            }

            return annotations.toArray( new Annotation[ annotations.size() ] );
        }

        @Override
        public Annotation[] getDeclaredAnnotations()
        {
            return new Annotation[ 0 ];
        }

        // @Override (Since JDK 8)
        @SuppressWarnings( "unchecked" )
        public <T extends Annotation> T[] getAnnotationsByType( Class<T> annotationClass )
        {
            Objects.requireNonNull( annotationClass, "annotationClass" );
            return (T[]) Array.newInstance( annotationClass, 0 );
        }

        // @Override (Since JDK 8)
        public <T extends Annotation> T getDeclaredAnnotation( Class<T> annotationClass )
        {
            Objects.requireNonNull( annotationClass, "annotationClass" );
            return null;
        }

        // @Override (Since JDK 8)
        @SuppressWarnings( "unchecked" )
        public <T extends Annotation> T[] getDeclaredAnnotationsByType( Class<T> annotationClass )
        {
            Objects.requireNonNull( annotationClass, "annotationClass" );
            return (T[]) Array.newInstance( annotationClass, 0 );
        }
    }
}
