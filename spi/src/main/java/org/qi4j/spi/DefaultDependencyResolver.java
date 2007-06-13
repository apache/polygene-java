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
package org.qi4j.spi;

import org.qi4j.api.DependencyResolver;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.model.CompositeContext;
import java.lang.reflect.Field;

public class DefaultDependencyResolver
    implements DependencyResolver
{
    public Object resolveField( Field field, CompositeContext context )
    {
        Class<?> type = field.getType();
        if( type.equals( CompositeFactory.class ) )
        {
            return context.getCompositeFactory();
        }
        else if( type.equals( FragmentFactory.class ) )
        {
            return context.getFragmentFactory();
        }
        else
        {
            return null;
        }
    }
}
