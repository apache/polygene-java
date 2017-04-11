/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.entity.Lifecycle;
import org.apache.polygene.api.entity.LifecycleException;
import org.apache.polygene.api.property.StateHolder;
import org.apache.polygene.bootstrap.BindingException;
import org.apache.polygene.runtime.composite.MixinModel;
import org.apache.polygene.runtime.composite.MixinsModel;
import org.apache.polygene.runtime.composite.UsesInstance;
import org.apache.polygene.runtime.injection.InjectionContext;
import org.apache.polygene.runtime.model.Resolution;

/**
 * JAVADOC
 */
public final class EntityMixinsModel
    extends MixinsModel
{
    private List<Integer> lifecycleMixins;

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
                    lifecycleMixins = new ArrayList<>();
                }

                lifecycleMixins.add( i );
            }
        }
    }

    Object newMixin( EntityInstance entityInstance, StateHolder state, Object[] mixins, Method method )
    {
        MixinModel model = methodImplementation.get( method );
        InjectionContext injectionContext = new InjectionContext( entityInstance, UsesInstance.EMPTY_USES, state );
        Object mixin = model.newInstance( injectionContext );
        mixins[ methodIndex.get( method ) ] = mixin;
        return mixin;
    }

    void invokeLifecycle( boolean create, Object[] mixins, CompositeInstance instance, StateHolder state )
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
                    try
                    {
                        lifecycle.create();
                    }
                    catch( Exception ex )
                    {
                        String message = "Unable to invoke create lifecycle on " + lifecycle;
                        throw new LifecycleException( message, ex );
                    }
                }
                else
                {
                    try
                    {
                        lifecycle.remove();
                    }
                    catch( Exception ex )
                    {
                        String message = "Unable to invoke remove lifecycle on " + lifecycle;
                        throw new LifecycleException( message, ex );
                    }
                }
            }
        }
    }
}
