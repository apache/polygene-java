/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.structure;

import java.util.Map;
import java.util.HashMap;
import org.qi4j.composite.Composite;

class MixinMapper
{
    private static final Class<Composite> DUMMY = Composite.class;

    static Map<Class, ModuleInstance> mapMixinsToModule( Class<? extends Composite> compositeType, ModuleInstance moduleInstance )
    {
        Map<Class, ModuleInstance> mapping = new HashMap<Class, ModuleInstance>();
        mapMixinsToModule( compositeType, moduleInstance, mapping );
        return mapping;
    }

    private static void mapMixinsToModule( Class type, ModuleInstance moduleInstance, Map<Class, ModuleInstance> mapping )
    {
        if( mapping.containsKey( type ) )
        {
            mapping.put( type, ModuleInstance.DUMMY );
        }
        else
        {
            mapping.put( type, moduleInstance );
        }

        for( Class subtype : type.getInterfaces() )
        {
            mapMixinsToModule( subtype, moduleInstance, mapping );
        }
    }

    static void mapMixinsToComposite( Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        mapMixinToComposite( compositeType, compositeType, mapping );
    }

    private static void mapMixinToComposite( Class type, Class<? extends Composite> compositeType, Map<Class, Class<? extends Composite>> mapping )
    {
        if( mapping.containsKey( type ) )
        {
            mapping.put( type, DUMMY );
        }
        else
        {
            mapping.put( type, compositeType );
        }

        for( Class subtype : type.getInterfaces() )
        {
            mapMixinToComposite( subtype, compositeType, mapping );
        }
    }
}
