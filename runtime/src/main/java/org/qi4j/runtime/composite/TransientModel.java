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
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.functional.Function;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.structure.ModuleInstance;

import java.lang.reflect.InvocationHandler;

/**
 * Model for Transient Composites
 */
public class TransientModel
    extends CompositeModel
    implements TransientDescriptor
{
    public TransientModel( final Class<?> compositeType,
                           Iterable<Class<?>> types, final Visibility visibility,
                           final MetaInfo metaInfo,
                           final MixinsModel mixinsModel,
                           final StateModel stateModel,
                           final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( compositeType, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    public CompositeInstance newCompositeInstance( ModuleInstance moduleInstance,
                                                   UsesInstance uses,
                                                   StateHolder state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        CompositeInstance compositeInstance = new TransientInstance( this, moduleInstance, mixins, state );

        // Instantiate all mixins
        int i = 0;
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        // Return
        return compositeInstance;
    }

    public StateHolder newBuilderState( final ModuleInstance module )
    {
        return stateModel.newBuilderInstance(new Function<PropertyDescriptor, Object>()
        {
            @Override
            public Object map( PropertyDescriptor propertyDescriptor )
            {
                return propertyDescriptor.initialValue( module );
            }
        } );
    }

    public StateHolder newState( StateHolder state )
    {
        return stateModel.newInstance( state );
    }

    @Override
    public String toString()
    {
        return type().getName();
    }
}