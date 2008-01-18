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
package org.qi4j.entity.memory;

import java.util.Map;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.entity.EntityComposite;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.EntityState;

public class MemoryEntityState
    implements EntityState
{
    private String identity;
    private Class<? extends EntityComposite> compositeType;
    private Map<String, Property> properties;
    private Map<String, AbstractAssociation> associations;

    public MemoryEntityState( String identity, Class<? extends EntityComposite> compositeType, Map<String, Property> properties, Map<String, AbstractAssociation> associations )
    {
        this.identity = identity;
        this.compositeType = compositeType;
        this.properties = properties;
        this.associations = associations;
    }

    public String getIdentity()
    {
        return identity;
    }

    public Class<? extends EntityComposite> getCompositeType()
    {
        return compositeType;
    }

    public Map<String, Property> getProperties()
    {
        return properties;
    }

    public Map<String, AbstractAssociation> getAssociations()
    {
        return associations;
    }

    public void refresh()
    {
    }
}
