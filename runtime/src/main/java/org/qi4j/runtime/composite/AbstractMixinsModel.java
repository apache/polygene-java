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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public class AbstractMixinsModel
{
    protected Set<MixinDeclaration> mixins = new LinkedHashSet<MixinDeclaration>();

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
            addMixinDeclarations( anInterface, mixins );
        }
    }

    // Model
    public MixinModel implementMethod( Method method )
    {
        if( !methodImplementation.containsKey( method ) )
        {
            Class mixinClass = findImplementation( method, mixins );
            if( mixinClass != null )
            {
                return implementMethodWithClass( method, mixinClass );
            }

            // Check declaring interface of method
            Set<MixinDeclaration> interfaceDeclarations = new LinkedHashSet<MixinDeclaration>();
            addMixinDeclarations( method.getDeclaringClass(), interfaceDeclarations );
            mixinClass = findImplementation( method, interfaceDeclarations );
            if( mixinClass != null )
            {
                return implementMethodWithClass( method, mixinClass );
            }

            throw new InvalidCompositeException( "No implementation found for method " + method.toGenericString(), compositeType );
        }
        else
        {
            Class mixinClass = methodImplementation.get( method );
            Integer index = mixinIndex.get( mixinClass );
            return mixinModels.get( index );
        }
    }

    private Class findImplementation( Method method, Set<MixinDeclaration> mixins )
    {
        for( MixinDeclaration mixin : mixins )
        {
            if( !mixin.isGeneric() && mixin.appliesTo( method, compositeType ) )
            {
                Class mixinClass = mixin.mixinClass();
                return mixinClass;
            }
        }
        for( MixinDeclaration mixin : mixins )
        {
            if( mixin.isGeneric() && mixin.appliesTo( method, compositeType ) )
            {
                Class mixinClass = mixin.mixinClass();
                return mixinClass;
            }
        }
        return null;
    }

    private MixinModel implementMethodWithClass( Method method, Class mixinClass )
    {
        methodImplementation.put( method, mixinClass );
        Integer index = mixinIndex.get( mixinClass );
        MixinModel mixinModel;
        if( index == null )
        {
            index = mixinIndex.size();
            mixinIndex.put( mixinClass, index );

            mixinModel = new MixinModel( mixinClass );
            mixinModels.add( mixinModel );
        }
        else
        {
            mixinModel = mixinModels.get( index );
        }
        methodIndex.put( method, index );
        return mixinModel;
    }

    private void addMixinDeclarations( Type type, Set<MixinDeclaration> declarations )
    {
        if( type instanceof Class )
        {
            Mixins annotation = Mixins.class.cast( ( (Class) type ).getAnnotation( Mixins.class ) );
            if( annotation != null )
            {
                Class[] mixinClasses = annotation.value();
                for( Class mixinClass : mixinClasses )
                {
                    declarations.add( new MixinDeclaration( mixinClass, type ) );
                }
            }
        }
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        for( MixinModel mixinModel : mixinModels )
        {
            mixinModel.visitModel( modelVisitor );
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
        final Object mixin = mixins[ methodIndex.get( methodInstance.method() ) ];
        return methodInstance.invoke( composite, params, mixin );
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

}
