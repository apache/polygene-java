/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.CompositeState;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite>
    implements CompositeBuilder<T>
{
    private Map<Class, Object> states;
    private CompositeFactory factory;
    private Class<T> compositeInterface;

    public CompositeBuilderImpl( CompositeFactory factory, Class<T> compositeInterface )
    {
        this.factory = factory;
        this.compositeInterface = compositeInterface;
        states = new HashMap<Class, Object>();
    }

    public T newInstance()
    {
        T composite = factory.newInstance( compositeInterface );
        CompositeState state = CompositeInvocationHandler.getInvocationHandler( composite );
        state.setMixins( states, false );
        return composite;
    }

    public <M> CompositeBuilder<T> set( Class<M> mixinType, M mixin )
    {
        states.put( mixinType, mixin );

        return this;
    }

    public <M> M get( Class<M> mixinType )
    {
        return mixinType.cast( states.get( mixinType ) );
    }

    public CompositeBuilder<T> adapt( Object mixin )
    {
        CompositeModel model = factory.getCompositeModel( compositeInterface );
        Set<Class> unresolved = model.getUnresolved();
        for( Class needed : unresolved )
        {
            if( needed.isInstance( mixin ) )
            {
                set( needed, needed.cast( mixin ) );
            }
        }

        return this;
    }
}
