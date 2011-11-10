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

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.MethodDescriptor;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.functional.Iterables.*;
import static org.qi4j.functional.Specifications.notNull;

/**
 * JAVADOC
 */
public final class CompositeMethodModel
    implements MethodDescriptor, Dependencies, VisitableHierarchy<Object, Object>
{
    // Model
    private Method method;
    private Method invocationMethod; // This will be the _ prefixed method on typed mixins
    private ConstraintsModel constraints;
    private ConcernsModel concerns;
    private SideEffectsModel sideEffects;
    private MixinsModel mixins;
    private AnnotatedElement annotations;

    // Context
//    private final SynchronizedCompositeMethodInstancePool instancePool = new SynchronizedCompositeMethodInstancePool();
    private final AtomicInstancePool instancePool = new AtomicInstancePool();
    private ConstraintsInstance constraintsInstance;

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
        initialize();
    }

    private void initialize()
    {
        annotations = new CompositeMethodAnnotatedElement();
        this.method.setAccessible( true );
//        instancePool = new SynchronizedCompositeMethodInstancePool();
    }

    // Model

    public Method method()
    {
        return method;
    }

    public MixinModel mixin()
    {
        return mixins.mixinFor( method );
    }

    public Iterable<DependencyModel> dependencies()
    {
        return flattenIterables( filter( notNull(), iterable( concerns != null ? concerns.dependencies() : null,
                                                              sideEffects != null ? sideEffects.dependencies() : null ) ) );
    }

    // Context
    public Object invoke( Object composite, Object[] params, MixinsInstance mixins, ModuleInstance moduleInstance )
        throws Throwable
    {
        constraintsInstance.checkValid( composite, method,  params );

        CompositeMethodInstance methodInstance = getInstance( moduleInstance );
        try
        {
            return mixins.invoke( composite, params, methodInstance );
        }
        finally
        {
            instancePool.returnInstance( methodInstance );
        }
    }

    private CompositeMethodInstance getInstance( ModuleInstance moduleInstance )
    {
        CompositeMethodInstance methodInstance = instancePool.getInstance();
        if( methodInstance == null )
        {
            methodInstance = newCompositeMethodInstance( moduleInstance );
        }

        return methodInstance;
    }

    private CompositeMethodInstance newCompositeMethodInstance( ModuleInstance moduleInstance )
        throws ConstructionException
    {
        FragmentInvocationHandler mixinInvocationHandler = mixins.newInvocationHandler( method );
        InvocationHandler invoker = mixinInvocationHandler;
        if( concerns != ConcernsModel.EMPTY_CONCERNS )
        {
            ConcernsInstance concernsInstance = concerns.newInstance( method, moduleInstance, mixinInvocationHandler );
            invoker = concernsInstance;
        }
        if( sideEffects != SideEffectsModel.EMPTY_SIDEEFFECTS )
        {
            SideEffectsInstance sideEffectsInstance = sideEffects.newInstance( method, moduleInstance, invoker );
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
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor ) throws ThrowableType
    {
        if (modelVisitor.visitEnter( this ))
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

    public class CompositeMethodAnnotatedElement
        implements AnnotatedElement
    {
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
                if( GenericSpecification.INSTANCE.satisfiedBy( model.mixinClass() ) )
                {
                    return false;
                }
                return ( model.mixinClass()
                             .getMethod( method.getName(), method.getParameterTypes() )
                             .isAnnotationPresent( annotationClass ) );
            }
            catch( NoSuchMethodException e )
            {
                return false;
            }
        }

        public <T extends Annotation> T getAnnotation( Class<T> annotationClass )
        {
            // Check mixin
            try
            {
                MixinModel model = mixins.mixinFor( method );
                if( !GenericSpecification.INSTANCE.satisfiedBy( model.mixinClass() ) )
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

        public Annotation[] getAnnotations()
        {
            // Add mixin annotations
            List<Annotation> annotations = new ArrayList<Annotation>();
            MixinModel model = mixins.mixinFor( method );
            Annotation[] mixinAnnotations = new Annotation[ 0 ];
            if( !GenericSpecification.INSTANCE.satisfiedBy( model.mixinClass() ) )
            {
                mixinAnnotations = model.mixinClass().getAnnotations();
                for( int i = 0; i < mixinAnnotations.length; i++ )
                {
                    annotations.add( mixinAnnotations[ i ] );
                }
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

        public Annotation[] getDeclaredAnnotations()
        {
            return new Annotation[ 0 ];
        }
    }
}
