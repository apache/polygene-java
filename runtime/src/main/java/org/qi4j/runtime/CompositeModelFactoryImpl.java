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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.model.CompositeModel;
import org.qi4j.api.model.NullArgumentException;

public class CompositeModelFactoryImpl
    implements CompositeModelFactory
{
    private CompositeModelBuilder compositeModelBuilder;

    public CompositeModelFactoryImpl( CompositeModelBuilder compositeModelBuilder )
    {
        this.compositeModelBuilder = compositeModelBuilder;
    }

    public <T extends Composite> CompositeModel<T> newCompositeModel( Class<T> compositeType )
    {
        if( compositeType == null )
        {
            throw new NullArgumentException( "compositeType" );
        }

        return compositeModelBuilder.getCompositeModel( compositeType);
    }
}
