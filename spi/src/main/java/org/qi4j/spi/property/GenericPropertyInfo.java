/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.spi.property;

import java.io.Serializable;
import java.util.HashMap;
import org.qi4j.property.PropertyInfo;

public final class GenericPropertyInfo
    implements PropertyInfo
{
    private HashMap<Class, Serializable> infos;
    private final String qualifiedName;
    private final String name;

    public GenericPropertyInfo( String qualifiedName )
    {
        this.qualifiedName = qualifiedName;
        this.name = PropertyModel.getName( qualifiedName );
        infos = new HashMap<Class, Serializable>();
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        Object o = infos.get( infoType );
        return infoType.cast( o );
    }

    public String getName()
    {
        return name;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public <T extends Serializable> void setPropertyInfo( Class<T> infoType, T instance )
    {
        synchronized( infos )
        {
            HashMap<Class, Serializable> clone = (HashMap<Class, Serializable>) infos.clone();
            clone.put( infoType, instance );
            infos = clone;
        }
    }
}
