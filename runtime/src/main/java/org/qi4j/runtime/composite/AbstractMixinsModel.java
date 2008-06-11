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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.State;
import org.qi4j.injection.scope.This;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.DependencyVisitor;
import org.qi4j.runtime.injection.InjectedFieldsModel;
import org.qi4j.runtime.injection.InjectedMethodsModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.Binder;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public class AbstractMixinsModel
{
    private List<MixinDeclaration> mixins = new ArrayList<MixinDeclaration>();

    private Map<Method, Class> methodImplementation = new HashMap<Method, Class>();
    protected List<MixinModel> mixinModels = new ArrayList<MixinModel>();
    private Map<Class, Integer> mixinIndex = new HashMap<Class, Integer>();
    private Map<Method, Integer> methodIndex = new HashMap<Method, Integer>();
    private Class<? extends Composite> compositeType;

    public AbstractMixinsModel( Class<? extends Composite> compositeType )
    {
        this.compositeType = compositeType;

        // Find mixin declarations
        mixins.add( new MixinDeclaration( CompositeMixin.class, Composite.class ) );
        Set<Type> interfaces = ClassUtil.interfacesOf( compositeType );

        for( Type anInterface : interfaces )
        {
            addMixinDeclarations( anInterface );
        }
    }

    // Model
    public void implementMethod( Method method )
    {
        if( !methodImplementation.containsKey( method ) )
        {
            for( MixinDeclaration mixin : mixins )
            {
                if( mixin.appliesTo( method, compositeType ) )
                {
                    Class mixinClass = mixin.mixinClass();
                    implementMethodWithClass( method, mixinClass );
                    return;
                }
            }
            throw new InvalidCompositeException( "No implementation found for method " + method.toGenericString(), compositeType );
        }
    }

    private void implementMethodWithClass( Method method, Class mixinClass )
    {
        methodImplementation.put( method, mixinClass );
        Integer index = mixinIndex.get( mixinClass );
        if( index == null )
        {
            index = mixinIndex.size();
            mixinIndex.put( mixinClass, index );

            if( !mixinClass.equals( CompositeInstance.class ) )
            {
                MixinModel mixinModel = new MixinModel( mixinClass );
                mixinModels.add( mixinModel );
            }
        }
        methodIndex.put( method, index );
    }

    private void addMixinDeclarations( Type type )
    {
        if( type instanceof Class )
        {
            Mixins annotation = Mixins.class.cast( ( (Class) type ).getAnnotation( Mixins.class ) );
            if( annotation != null )
            {
                Class[] mixinClasses = annotation.value();
                for( Class mixinClass : mixinClasses )
                {
                    mixins.add( new MixinDeclaration( mixinClass, type ) );
                }
            }
        }
    }

    public void implementThisUsing( CompositeModel compositeModel )
    {
        Set<Class> thisMixinTypes = new HashSet<Class>();
        for( MixinModel mixinModel : mixinModels )
        {
            thisMixinTypes.addAll( mixinModel.thisMixinTypes() );
        }

        for( Class thisMixinType : thisMixinTypes )
        {
            compositeModel.implementMixinType( thisMixinType );
        }
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( MixinModel mixinModel : mixinModels )
        {
            mixinModel.visitDependencies( visitor );
        }
    }

    // Binding
    public void bind( Resolution resolution ) throws BindingException
    {
        for( MixinModel mixinComposite : mixinModels )
        {
            mixinComposite.bind( resolution );
        }
    }

    // Context
    public Object[] newMixinHolder()
    {
        return new Object[mixinIndex.size()];
    }

    public Object invoke( Object composite, Object[] params, Object[] mixins, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        return methodInstance.invoke( composite, params, mixins[ methodIndex.get( methodInstance.method() ) ] );
    }

    public FragmentInvocationHandler newInvocationHandler( final Method method )
    {
        return mixinFor( method ).newInvocationHandler( method.getDeclaringClass() );
    }

    private MixinModel mixinFor( Method method )
    {
        Integer integer = methodIndex.get( method );
        return mixinModels.get( integer );
    }

    /**
     * TODO
     */
    protected static final class MixinModel
        implements Binder
    {
        // Model
        private Class mixinClass;
        private ConstructorsModel constructorsModel;
        private InjectedFieldsModel injectedFieldsModel;
        private InjectedMethodsModel injectedMethodsModel;

        private MixinModel( Class mixinClass )
        {
            this.mixinClass = mixinClass;

            constructorsModel = new ConstructorsModel( mixinClass );
            injectedFieldsModel = new InjectedFieldsModel( mixinClass );
            injectedMethodsModel = new InjectedMethodsModel( mixinClass );
        }

        public void visitDependencies( DependencyVisitor visitor )
        {
            constructorsModel.visitDependencies( visitor );
            injectedFieldsModel.visitDependencies( visitor );
            injectedMethodsModel.visitDependencies( visitor );
        }

        // Binding
        public void bind( Resolution context ) throws BindingException
        {
            constructorsModel.bind( context );
            injectedFieldsModel.bind( context );
            injectedMethodsModel.bind( context );
        }

        // Context
        public Object newInstance( CompositeInstance compositeInstance, UsesInstance uses, State state )
        {
            InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
            Object mixin = constructorsModel.newInstance( injectionContext );
            injectedFieldsModel.inject( injectionContext, mixin );
            injectedMethodsModel.inject( injectionContext, mixin );
            return mixin;
        }

        private Set<Class> thisMixinTypes()
        {
            final Set<Class> mixinTypes = new HashSet<Class>();

            DependencyVisitor visitor = new DependencyVisitor()
            {
                public void visit( DependencyModel dependencyModel, Resolution resolution )
                {
                    if( dependencyModel.injectionAnnotation().annotationType().equals( This.class ) )
                    {
                        mixinTypes.add( dependencyModel.injectionClass() );
                    }
                }
            };

            visitDependencies( visitor );

            return mixinTypes;
        }

        protected FragmentInvocationHandler newInvocationHandler( Class methodClass )
        {
            if( InvocationHandler.class.isAssignableFrom( mixinClass ) && !methodClass.isAssignableFrom( mixinClass ) )
            {
                return new GenericFragmentInvocationHandler();
            }
            else
            {
                return new TypedFragmentInvocationHandler();
            }

        }

    }
}
