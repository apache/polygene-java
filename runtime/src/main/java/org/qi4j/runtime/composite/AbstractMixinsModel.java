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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.InvalidCompositeException;
import org.qi4j.spi.util.MethodKeyMap;
import org.qi4j.spi.util.UsageGraph;

/**
 * Base implementation of model for mixins. This records the mapping between methods in the Composite
 * and mixin implementations.
 */
public abstract class AbstractMixinsModel
    implements Serializable, Binder
{
    protected final Set<MixinDeclaration> mixins = new LinkedHashSet<MixinDeclaration>();

    protected final Map<Method, MixinModel> methodImplementation = new MethodKeyMap<MixinModel>();
    protected final Map<Method, Integer> methodIndex = new MethodKeyMap<Integer>();
    private final Class<? extends Composite> compositeType;
    protected List<MixinModel> mixinModels = new ArrayList<MixinModel>();

    private final Map<Class, Integer> mixinIndex = new HashMap<Class, Integer>();
    private final Set<Class> mixinTypes = new HashSet<Class>();

    public AbstractMixinsModel( Class<? extends Composite> compositeType, List<Class<?>> assemblyMixins )
    {
        this.compositeType = compositeType;

        // Add assembly mixins
        for( Class<?> assemblyMixin : assemblyMixins )
        {
            this.mixins.add( new MixinDeclaration( assemblyMixin, Assembler.class ) );
        }

        // Find mixin declarations
        this.mixins.add( new MixinDeclaration( CompositeMixin.class, Composite.class ) );
        Set<Class> interfaces = Classes.interfacesOf( compositeType );

        for( Class anInterface : interfaces )
        {
            addMixinDeclarations( anInterface, this.mixins );
            mixinTypes.add( anInterface );
        }
    }

    // Model

    public Iterable<Class> mixinTypes()
    {
        return mixinTypes;
    }

    public Iterable<Method> mixinMethods()
    {
        return Collections.unmodifiableSet( methodImplementation.keySet() );
    }

    public boolean hasMixinType( Class<?> mixinType )
    {
        for( Class type : mixinTypes )
        {
            if( mixinType.isAssignableFrom( type ) )
            {
                return true;
            }
        }

        return false;
    }

    public MixinModel mixinFor( Method method )
    {
        Integer integer = methodIndex.get( method );
        return mixinModels.get( integer );
    }

    public MixinModel implementMethod( Method method )
    {
        MixinModel implementationModel = methodImplementation.get( method );
        if( implementationModel != null )
        {
            return implementationModel;
        }
        Class mixinClass = findTypedImplementation( method, mixins );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check declaring interface of method
        Set<MixinDeclaration> interfaceDeclarations = new LinkedHashSet<MixinDeclaration>();
        addMixinDeclarations( method.getDeclaringClass(), interfaceDeclarations );
        mixinClass = findTypedImplementation( method, interfaceDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check generic implementations
        mixinClass = findGenericImplementation( method, mixins );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check declaring interface of method
        mixinClass = findGenericImplementation( method, interfaceDeclarations );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        throw new InvalidCompositeException( "No implementation found for method " + method.toGenericString(), compositeType );
    }

    public void addMixinType( Class mixinType )
    {
        mixinTypes.add( mixinType );
    }

    private Class findTypedImplementation( Method method, Set<MixinDeclaration> mixins )
    {
        for( MixinDeclaration mixin : mixins )
        {
            if( !mixin.isGeneric() && mixin.appliesTo( method, compositeType ) )
            {
                Class mixinClass = mixin.mixinClass();
                return mixinClass;
            }
        }
        return null;
    }

    private Class findGenericImplementation( Method method, Set<MixinDeclaration> mixins )
    {
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
            final Class clazz = (Class) type;
            Mixins annotation = Mixins.class.cast( clazz.getAnnotation( Mixins.class ) );
            if( annotation != null )
            {
                Class[] mixinClasses = annotation.value();
                for( Class mixinClass : mixinClasses )
                {
                    declarations.add( new MixinDeclaration( mixinClass, clazz ) );
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

    public void bind( Resolution resolution )
        throws BindingException
    {
        // Order mixins based on @This usages
        UsageGraph<MixinModel> deps = new UsageGraph<MixinModel>( mixinModels, new Uses(), true );
        mixinModels = deps.resolveOrder();

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

    public Object getMixin( Object[] mixins, Method method )
    {
        return mixins[ methodIndex.get( method ) ];
    }

    public FragmentInvocationHandler newInvocationHandler( final Method method )
    {
        return mixinFor( method ).newInvocationHandler( method );
    }

    public void activate( Object[] mixins )
        throws Exception
    {
        int idx = 0;
        try
        {
            for( MixinModel mixinModel : mixinModels )
            {
                mixinModel.activate( mixins[ idx ] );
                idx++;
            }
        }
        catch( Exception e )
        {
            // Passivate activated mixins
            for( int i = idx - 1; i >= 0; i-- )
            {
                try
                {
                    mixinModels.get( i ).passivate( i );
                }
                catch( Exception e1 )
                {
                    // Ignore
                }
            }

            throw e;
        }
    }

    public void passivate( Object[] mixins )
        throws Exception
    {
        int idx = 0;
        Exception ex = null;
        for( MixinModel mixinModel : mixinModels )
        {
            try
            {
                mixinModel.passivate( mixins[ idx++ ] );
            }
            catch( Exception e )
            {
                ex = e;
            }
        }
        if( ex != null )
        {
            throw ex;
        }
    }

    private class Uses
        implements UsageGraph.Use<MixinModel>
    {
        public Collection<MixinModel> uses( MixinModel source )
        {
            Set<Class> thisMixinTypes = source.thisMixinTypes();
            List<MixinModel> usedMixinClasses = new ArrayList<MixinModel>();
            for( Class thisMixinType : thisMixinTypes )
            {
                for( Method method : thisMixinType.getMethods() )
                {
                    usedMixinClasses.add( methodImplementation.get( method ) );
                }
            }
            return usedMixinClasses;
        }
    }
}
