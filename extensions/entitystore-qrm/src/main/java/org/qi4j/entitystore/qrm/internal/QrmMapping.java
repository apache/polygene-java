/*  Copyright 2009 Alex Shneyderman
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
package org.qi4j.entitystore.qrm.internal;

import org.qi4j.api.entity.EntityDescriptor;

import java.util.ArrayList;
import java.util.List;

public class QrmMapping
{

    private List<QrmProperty> properties = new ArrayList<QrmProperty>();

    private EntityDescriptor entityDescriptor = null;

    public QrmMapping( EntityDescriptor entityDescriptor )
    {
        this.entityDescriptor = entityDescriptor;

        properties.add( new QrmProperty.Default( "updatedOn", "timestamp", false ) );
        properties.add( new QrmProperty.Default( "createdOn", "timestamp", false ) );
    }

    public Iterable<QrmProperty> properties()
    {
        return properties;
    }

    public void addProperty( String name, String hibernateType, boolean nullable )
    {
        properties.add( new QrmProperty.Default( name, hibernateType, nullable ) );
    }

    public void addIdentity( String name, String column, String hibernateType )
    {
        properties.add( new QrmProperty.Default( name, column, hibernateType, false, true ) );
    }

    public QrmProperty identity()
    {
        for( QrmProperty property : properties )
        {
            if( property.isIdentity() )
            {
                return property;
            }
        }

        return null;
    }

    public QrmProperty updatedOn()
    {
        for( QrmProperty property : properties )
        {
            if( property.name().equals( "updatedOn" ) )
            {
                return property;
            }
        }

        return null;
    }

    public QrmProperty createdOn()
    {
        for( QrmProperty property : properties )
        {
            if( property.name().equals( "createdOn" ) )
            {
                return property;
            }
        }

        return null;
    }

    public EntityDescriptor getEntityDescriptor()
    {
        return entityDescriptor;
    }
}
