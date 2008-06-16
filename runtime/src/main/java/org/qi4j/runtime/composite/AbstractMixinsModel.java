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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
public abstract class AbstractMixinsModel
{
    protected final Set<MixinDeclaration> mixins = new LinkedHashSet<MixinDeclaration>();

    private final Map<Method, MixinModel> methodImplementation = new HashMap<Method, MixinModel>();
    protected final List<MixinModel> mixinModels = new ArrayList<MixinModel>();
    private final Map<Class, Integer> mixinIndex = new HashMap<Class, Integer>();
    private final Map<Method, Integer> methodIndex = new HashMap<Method, Integer>();
    private final Class<? extends Composite> compositeType;
    private Set<Class> mixinTypes = new HashSet<Class>();

    public AbstractMixinsModel( Class<? extends Composite> compositeType )
    {
        this.compositeType = compositeType;

        // Find mixin declarations
        mixins.add( new MixinDeclaration( CompositeMixin.class, Composite.class ) );
        Set<Type> interfaces = ClassUtil.interfacesOf( compositeType );

        for( Type anInterface : interfaces )
        {
            addMixinDeclarations( anInterface, mixins );
            mixinTypes.add( (Class) anInterface );
        }
    }

    // Model
    public Iterable<Class> mixinTypes()
    {
        return mixinTypes;
    }

    public MixinModel implementMethod( Method method )
    {
        if( !methodImplementation.containsKey( method ) )
        {
            mixinTypes.add( method.getDeclaringClass() );

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
            return methodImplementation.get( method );
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
        MixinModel foundMixinModel = null;

        for( MixinModel mixinModel : mixinModels )
        {
            if( mixinModel.mixinClass().equals( mixinClass ) )
            {
                foundMixinModel = mixinModel;
                break;
            }
        }

        if( foundMixinModel == null )
        {
            foundMixinModel = new MixinModel( mixinClass );
            mixinModels.add( foundMixinModel );
        }

        methodImplementation.put( method, foundMixinModel );

        return foundMixinModel;
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
        // Order mixins based on @This usages
        Collections.sort( mixinModels, new Comparator<MixinModel>()
        {
            public int compare( MixinModel mixinModel, MixinModel mixinModel1 )
            {
                if( thisUses( mixinModel, mixinModel1 ) )
                {
                    if( thisUses( mixinModel1, mixinModel ) )
                    {
                        throw new InvalidCompositeException( "Cyclic @This injection dependencies between " + mixinModel.mixinClass().getName() + " and " + mixinModel1.mixinClass().getName(), compositeType );
                    }
                    else
                    {
                        return 1;
                    }
                }
                else if( thisUses( mixinModel1, mixinModel ) )
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        } );

        // Populate mappings
        for( int i = 0; i < mixinModels.size(); i++ )
        {
            MixinModel mixinModel = mixinModels.get( i );
            mixinIndex.put( mixinModel.mixinClass(), i );
        }

        for( Map.Entry<Method, MixinModel> methodClassEntry : methodImplementation.entrySet() )
        {
            methodIndex.put( methodClassEntry.getKey(), mixinIndex.get( methodClassEntry.getValue().mixinClass() ) );
        }


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

    private boolean thisUses( MixinModel mixinModel, MixinModel mixinModel1 )
    {
        Set<Class> thisMixinTypes = mixinModel.thisMixinTypes();
        Set<Class> usedMixinClasses = new HashSet<Class>();
        for( Class thisMixinType : thisMixinTypes )
        {
            for( Method method : thisMixinType.getMethods() )
            {
                usedMixinClasses.add( methodImplementation.get( method ).mixinClass() );
            }
        }

        return ( usedMixinClasses.contains( mixinModel1.mixinClass() ) );
    }
}
