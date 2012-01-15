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

import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.functional.Function;
import org.qi4j.functional.HierarchicalVisitor;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.VisitableHierarchy;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectedFieldModel;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static org.qi4j.functional.Iterables.*;

/**
 * Base implementation of model for mixins. This records the mapping between methods in the Composite
 * and mixin implementations.
 */
public class MixinsModel
        implements Binder, VisitableHierarchy<Object, Object>
{
    protected final Map<Method, MixinModel> methodImplementation = new HashMap<Method, MixinModel>();
    protected final Map<Method, Integer> methodIndex = new HashMap<Method, Integer>();
    protected List<MixinModel> mixinModels = new ArrayList<MixinModel>();

    private final Map<Class, Integer> mixinIndex = new HashMap<Class, Integer>();
    private final Set<Class<?>> mixinTypes = new HashSet<Class<?>>();

    public Iterable<Class<?>> mixinTypes()
    {
        return mixinTypes;
    }

    public <T> boolean isImplemented( Class<T> mixinType )
    {
        return mixinTypes.contains( mixinType );
    }

    public List<MixinModel> mixinModels()
    {
        return mixinModels;
    }

    public MixinModel mixinFor( Method method )
    {
        return methodImplementation.get( method );
    }
    
    public MixinModel getMixinModel(Class mixinClass)
    {
        for( MixinModel mixinModel : mixinModels )
        {
            if (mixinModel.mixinClass().equals( mixinClass ))
                return mixinModel;
        }
        return null;
    }

    public void addMixinType( Class mixinType )
    {
        for( Type type : Classes.INTERFACES_OF.map( mixinType ) )
        {
            mixinTypes.add( Classes.RAW_CLASS.map( type ) );
        }
    }

    public void addMixinModel(MixinModel mixinModel)
    {
        mixinModels.add( mixinModel );
    }

    public void addMethodMixin(Method method, MixinModel mixinModel)
    {
        methodImplementation.put( method, mixinModel);
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor ) throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( MixinModel mixinModel : mixinModels )
            {
                mixinModel.accept( visitor );
            }
        }
        return visitor.visitLeave( this );
    }

    // Binding
    public void bind( final Resolution resolution )
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

        for( MixinModel mixinModel : mixinModels )
        {
            mixinModel.accept( new HierarchicalVisitorAdapter<Object, Object, BindingException>()
            {
                @Override
                public boolean visitEnter( Object visited ) throws BindingException
                {
                    if( visited instanceof InjectedFieldModel )
                    {
                        InjectedFieldModel fieldModel = (InjectedFieldModel) visited;
                        fieldModel.bind( resolution.forField( fieldModel.field() ) );
                        return false;
                    } else if( visited instanceof Binder )
                    {
                        Binder constructorsModel = (Binder) visited;
                        constructorsModel.bind( resolution );

                        return false;
                    }
                    return true;
                }

                @Override
                public boolean visit( Object visited ) throws BindingException
                {
                    if( visited instanceof Binder )
                    {
                        ((Binder) visited).bind( resolution );
                    }
                    return true;
                }
            } );
        }
    }

    // Context

    public Object[] newMixinHolder()
    {
        return new Object[mixinIndex.size()];
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
                mixinModel.activate( mixins[idx] );
                idx++;
            }
        } catch( Exception e )
        {
            // Passivate activated mixins
            for( int i = idx - 1; i >= 0; i-- )
            {
                try
                {
                    mixinModels.get( i ).passivate( i );
                } catch( Exception e1 )
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
                mixinModel.passivate( mixins[idx++] );
            } catch( Exception e )
            {
                ex = e;
            }
        }
        if( ex != null )
        {
            throw ex;
        }
    }

    public Iterable<DependencyModel> dependencies()
    {
        return flattenIterables( map( new Function<MixinModel, Iterable<DependencyModel>>()
                {
                    @Override
                    public Iterable<DependencyModel> map( MixinModel mixinModel )
                    {
                        return mixinModel.dependencies();
                    }
                }, mixinModels ) );
    }

    private class Uses
            implements UsageGraph.Use<MixinModel>
    {
        public Collection<MixinModel> uses( MixinModel source )
        {
            Iterable<Class<?>> thisMixinTypes = source.thisMixinTypes();
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
