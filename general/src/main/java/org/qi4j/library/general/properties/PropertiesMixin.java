/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.general.properties;

import org.qi4j.api.annotation.AppliesTo;
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.beans.Beans;
import java.beans.Introspector;

/**
 * Generic property mixin. Methods in interface
 * can be of the following types:
 * setFoo = set property named foo
 * getFoo = get property named foo
 * addFoo = add object to list named foo
 * removeFoo = remove object from list named foo 
 *
 * @author rickard
 * @version $Revision: 1.0 $
 */
@AppliesTo( Serializable.class)
public class PropertiesMixin
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    Map properties = new HashMap();

    // InvocationHandler implementation ------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        String methodName = method.getName();
        if ( methodName.startsWith("set"))
        {
            // Setter
            String name = Introspector.decapitalize( methodName.substring( 3 ) );
            properties.put( name, args[0]);
            return null;
        } else if (methodName.startsWith( "get"))
        {
            // Getter
            String name = Introspector.decapitalize( methodName.substring( 3 ) );
            return properties.get(name);
        } else if (methodName.startsWith( "add"))
        {
            // Add to list
            String name = Introspector.decapitalize( methodName.substring( 3 ) );
            List list = (List) properties.get( name);
            if (list == null)
            {
                list = new ArrayList();
                properties.put( name, list);
            }
            list.add( args[0]);
        } else if (methodName.startsWith( "remove"))
        {
            // Remove from list
            String name = Introspector.decapitalize( methodName.substring( 6 ) );
            List list = (List) properties.get( name);
            if (list == null)
            {
                return null;
            }
            list.remove( args[0]);
        }

        return null;
    }
}
