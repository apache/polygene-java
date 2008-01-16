/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.structure;

import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.runtime.structure.TypeMapper;

/**
 * CBF that delegates to a number of other factories. This is used to implement
 * instantiation of public Composites in other Modules.
 */
public final class CompositeBuilderFactoryStrategy
    implements CompositeBuilderFactory, TypeMapper
{
    private Map<Class<? extends Composite>, CompositeBuilderFactory> factories;
    private HashMap<Class, Class<? extends Composite>> pojoMap;

    public CompositeBuilderFactoryStrategy( Map<Class<? extends Composite>, CompositeBuilderFactory> factories )
    {
        this.factories = factories;
        pojoMap = new HashMap<Class, Class<? extends Composite>>();
    }

    public <T extends Composite> CompositeBuilder<T> newCompositeBuilder( Class<T> compositeType )
    {
        CompositeBuilderFactory factory = factories.get( compositeType );
        CompositeBuilder<T> builder = factory.newCompositeBuilder( compositeType );
        return builder;
    }

    public <T> T newComposite( Class<T> pojoType )
    {
        Class<? extends Composite> compositeType = pojoMap.get( pojoType );
        return pojoType.cast( newCompositeBuilder( compositeType ).newInstance() );
    }

    public void registerComposite( Class<? extends Composite> compositeType )
    {
        for( Class type : compositeType.getInterfaces())
        {
            if( type.equals( Serializable.class ) )
            {
            }
            else if( pojoMap.containsKey( type ) )
            {
                pojoMap.remove( type );
            }
            else
            {
                pojoMap.put( type, compositeType );
            }
        }
    }

    public void unregisterComposite( Class<? extends Composite> compositeType )
    {
        for( Class type : compositeType.getInterfaces())
        {
            pojoMap.remove( type );
        }
    }
}
