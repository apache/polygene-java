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
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.MixinBuilder;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.CompositeModel;

public class MixinBuilderImpl<T extends Composite>
    implements MixinBuilder<T>
{
    protected Map<Class, Object> states;
    private CompositeModelFactory modelFactory;
    protected CompositeBuilderFactoryImpl builderFactory;
    protected Class<T> compositeInterface;
    private FragmentFactory fragmentFactory;

    MixinBuilderImpl( FragmentFactory fragmentFactory, CompositeModelFactory modelFactory, CompositeBuilderFactoryImpl builderFactory, Class<T> compositeInterface )
    {
        this.fragmentFactory = fragmentFactory;
        this.modelFactory = modelFactory;
        this.builderFactory = builderFactory;
        this.compositeInterface = compositeInterface;
        states = new HashMap<Class, Object>();
    }

    public <M> void setMixin( Class<M> mixinType, M mixin )
    {
        states.put( mixinType, mixin );
    }

    public <M> M getMixin( Class<M> mixinType )
    {
        Object mixin = states.get( mixinType );
        if( mixin == null )
        {
            CompositeModel model = modelFactory.getCompositeModel( compositeInterface );
            List<MixinModel> mixinModels = model.getImplementations( mixinType );
            if( mixinModels.size() == 0 )
            {
                return null;
            }
            MixinModel mixinModel = mixinModels.get( 0 );
            mixin = fragmentFactory.newFragment( mixinModel, model );
            states.put( mixinType, mixin );
        }
        return mixinType.cast( mixin );
    }

    public void adapt( Object mixin )
    {
        CompositeModel model = modelFactory.getCompositeModel( compositeInterface );
        Set<Class> unresolved = model.getUnresolved();
        for( Class needed : unresolved )
        {
            if( needed.isInstance( mixin ) )
            {
                setMixin( needed, needed.cast( mixin ) );
            }
        }
    }
}
