/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

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
import org.qi4j.runtime.composite.CompositeMethodInstance;
import org.qi4j.runtime.composite.CompositeMixin;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public final class MixinsModel
{
    private List<MixinDeclaration> mixins = new ArrayList<MixinDeclaration>();

    private Map<Method, Class> methodImplementation = new HashMap<Method, Class>();
    private List<MixinModel> mixinModels = new ArrayList<MixinModel>();
    private Map<Class, Integer> mixinIndex = new HashMap<Class, Integer>();
    private Map<Method, Integer> methodIndex = new HashMap<Method, Integer>();
    private Class<? extends Composite> compositeType;

    public MixinsModel( Class<? extends Composite> compositeType )
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
                    methodImplementation.put( method, mixinClass );
                    Integer index = mixinIndex.get( mixinClass );
                    if( index == null )
                    {
                        index = mixinIndex.size();
                        mixinIndex.put( mixinClass, index );

                        MixinModel mixinComposite = new MixinModel( mixinClass );
                        mixinModels.add( mixinComposite );
                    }
                    methodIndex.put( method, index );
                    return;
                }
            }

            throw new InvalidCompositeException( "No implementation found for method " + method.toGenericString(), compositeType );
        }
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

    // Binding
    public void bind( BindingContext bindingContext )
    {
        for( MixinModel mixinComposite : mixinModels )
        {
            mixinComposite.bind( bindingContext );
        }
    }

    public MixinModel mixinFor( Method method )
    {
        Integer integer = methodIndex.get( method );
        return mixinModels.get( integer );
    }

    // Context
    public Object[] newMixinHolder()
    {
        return new Object[mixinIndex.size()];
    }

    public Object invoke( Object composite, Object[] params, Object[] mixins, CompositeMethodInstance methodInstance )
        throws Throwable
    {
        return methodInstance.invoke( composite, params, mixins[ methodIndex.get( methodInstance.getMethod() ) ] );
    }

    public void newMixins( CompositeInstance compositeInstance, Set<Object> uses, State state, Object[] mixins )
    {
        int i = 0;
        for( MixinModel mixinModel : mixinModels )
        {
            Object mixin = mixinModel.newInstance( compositeInstance, uses, state );
            mixins[ i++ ] = mixin;
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
}
