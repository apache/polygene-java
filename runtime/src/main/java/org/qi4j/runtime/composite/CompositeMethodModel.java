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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.DependencyVisitor;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeMethodDescriptor;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public final class CompositeMethodModel
    implements Binder, CompositeMethodDescriptor, Serializable
{
    // Model
    private Method method;
    private MethodConstraintsModel methodConstraints;
    private MethodConcernsModel methodConcerns;
    private MethodSideEffectsModel methodSideEffects;
    private AbstractMixinsModel mixins;
    private AnnotatedElement annotations;

    // Context
    private SynchronizedCompositeMethodInstancePool instancePool;
    //    private final CompositeMethodInstancePool instancePool = new AtomicCompositeMethodInstancePool();
    //    private final CompositeMethodInstancePool instancePool = new ThreadLocalCompositeMethodInstancePool();
    private MethodConstraintsInstance methodConstraintsInstance;

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        try
        {
            SerializationUtil.writeMethod( out, method );
            out.writeObject( mixins );
            out.writeObject( methodConcerns );
            out.writeObject( methodSideEffects );
            out.writeObject( methodConstraints );
        }
        catch( NotSerializableException e )
        {
            System.err.println( "NotSerializable in " + getClass() );
            throw e;
        }
    }

    private void readObject( ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        this.method = SerializationUtil.readMethod( in );
        mixins = (AbstractMixinsModel) in.readObject();
        methodConcerns = (MethodConcernsModel) in.readObject();
        methodSideEffects = (MethodSideEffectsModel) in.readObject();
        methodConstraints = (MethodConstraintsModel) in.readObject();
        initialize();
    }

    public CompositeMethodModel( Method method,
                                 MethodConstraintsModel methodConstraintsModel,
                                 MethodConcernsModel methodConcernsModel,
                                 MethodSideEffectsModel methodSideEffectsModel,
                                 AbstractMixinsModel mixinsModel
    )
    {
        this.method = method;
        mixins = mixinsModel;
        methodConcerns = methodConcernsModel;
        methodSideEffects = methodSideEffectsModel;
        methodConstraints = methodConstraintsModel;
        initialize();
    }

    private void initialize()
    {
        annotations = new CompositeMethodAnnotatedElement();
        this.method.setAccessible( true );
        instancePool = new SynchronizedCompositeMethodInstancePool();
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

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        resolution = new Resolution( resolution.application(),
                                     resolution.layer(),
                                     resolution.module(),
                                     resolution.object(),
                                     this,
                                     null //no field
        );

        methodConcerns.bind( resolution );
        methodSideEffects.bind( resolution );

        methodConstraintsInstance = methodConstraints.newInstance();
    }

    // Context

    public Object invoke( Object composite, Object[] params, MixinsInstance mixins, ModuleInstance moduleInstance )
        throws Throwable
    {
        methodConstraintsInstance.checkValid( composite, params );

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
        if( methodConcerns.hasConcerns() )
        {
            MethodConcernsInstance concernsInstance = methodConcerns.newInstance( moduleInstance, mixinInvocationHandler );
            invoker = concernsInstance;
        }
        if( methodSideEffects.hasSideEffects() )
        {
            MethodSideEffectsInstance sideEffectsInstance = methodSideEffects.newInstance( moduleInstance, invoker );
            invoker = sideEffectsInstance;
        }

        return new CompositeMethodInstance( invoker, mixinInvocationHandler, method, mixins.methodIndex.get( method ) );
    }

    public AnnotatedElement annotatedElement()
    {
        return annotations;
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );

        methodConstraints.visitModel( modelVisitor );
        methodConcerns.visitModel( modelVisitor );
        methodSideEffects.visitModel( modelVisitor );
    }

    public void addThisInjections( final Set<Class> thisDependencies )
    {
        visitModel(
            new DependencyVisitor( new DependencyModel.ScopeSpecification( This.class ) )
            {
                public void visitDependency( DependencyModel dependencyModel )
                {
                    thisDependencies.add( dependencyModel.rawInjectionType() );
                }
            }
        );
    }

    @Override
    public String toString()
    {
        return method.toGenericString();
    }

    public class CompositeMethodAnnotatedElement
        implements AnnotatedElement, Serializable
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
                if( model.isGeneric() )
                {
                    return false;
                }
                return ( model.mixinClass()
                    .getMethod( method.getName(), method.getParameterTypes() ).isAnnotationPresent( annotationClass ) );
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
                if( !model.isGeneric() )
                {
                    T annotation = annotationClass.cast( model.mixinClass()
                        .getMethod( method.getName(), method.getParameterTypes() ).getAnnotation( annotationClass ) );
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
            Annotation[] mixinAnnotations = new Annotation[0];
            if( !model.isGeneric() )
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

            return annotations.toArray( new Annotation[annotations.size()] );
        }

        public Annotation[] getDeclaredAnnotations()
        {
            return new Annotation[0];
        }
    }
}
