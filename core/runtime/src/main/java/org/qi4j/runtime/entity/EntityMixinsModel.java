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

package org.qi4j.runtime.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.property.StateHolder;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class EntityMixinsModel
    extends MixinsModel
{
    List<Integer> lifecycleMixins;

    @Override
    public void bind( Resolution resolution )
        throws BindingException
    {
        super.bind( resolution );

        // Find what mixins implement Lifecycle
        for( int i = 0; i < mixinModels.size(); i++ )
        {
            MixinModel mixinModel = mixinModels.get( i );
            if( Lifecycle.class.isAssignableFrom( mixinModel.mixinClass() ) )
            {
                if( lifecycleMixins == null )
                {
                    lifecycleMixins = new ArrayList<Integer>();
                }

                lifecycleMixins.add( i );
            }
        }
    }

    public Object newMixin( EntityInstance entityInstance, StateHolder state, Object[] mixins, Method method )
    {
        MixinModel model = methodImplementation.get( method );
        InjectionContext injectionContext = new InjectionContext( entityInstance, UsesInstance.EMPTY_USES, state );
        Object mixin = model.newInstance( injectionContext );
        mixins[ methodIndex.get( method ) ] = mixin;
        return mixin;
    }

    public void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
    {
        if( lifecycleMixins != null )
        {
            InjectionContext injectionContext = new InjectionContext( instance, UsesInstance.EMPTY_USES, state );
            for( Integer lifecycleMixin : lifecycleMixins )
            {
                Lifecycle lifecycle = (Lifecycle) mixins[ lifecycleMixin ];

                if( lifecycle == null )
                {
                    lifecycle = (Lifecycle) mixinModels.get( lifecycleMixin ).newInstance( injectionContext );
                }

                if( create )
                {
                    lifecycle.create();
                }
                else
                {
                    lifecycle.remove();
                }
            }
        }
    }
}
