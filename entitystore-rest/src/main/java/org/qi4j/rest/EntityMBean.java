/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;

/**
 * JAVADOC
 */
public class EntityMBean
    implements DynamicMBean
{
    private EntityComposite entity;
    private MBeanInfo mbeanInfo;

    public EntityMBean( EntityComposite entity, MBeanInfo mbeanInfo )
    {
        this.entity = entity;
        this.mbeanInfo = mbeanInfo;
    }

    public Object getAttribute( String s )
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        try
        {
            Property property = (Property) entity.getClass().getMethod( s, new Class[0] ).invoke( entity );
            return property.get();
        }
        catch( Exception e )
        {
            throw new AttributeNotFoundException( s );
        }
    }

    public void setAttribute( Attribute attribute )
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        try
        {
            Property property = (Property) entity.getClass()
                .getMethod( attribute.getName(), new Class[0] )
                .invoke( entity );
            property.set( attribute.getValue() );
        }
        catch( Exception e )
        {
            throw new AttributeNotFoundException( attribute.getName() );
        }
    }

    public AttributeList getAttributes( String[] strings )
    {
        AttributeList attributes = new AttributeList();
        for( String string : strings )
        {
            try
            {
                attributes.add( new Attribute( string, getAttribute( string ) ) );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        return attributes;
    }

    public AttributeList setAttributes( AttributeList attributeList )
    {
        for( Object attribute : attributeList )
        {
            Attribute attr = (Attribute) attribute;
            try
            {
                setAttribute( attr );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
        return attributeList;
    }

    public Object invoke( String s, Object[] objects, String[] strings )
        throws MBeanException, ReflectionException
    {
        return null;
    }

    public MBeanInfo getMBeanInfo()
    {
        return mbeanInfo;
    }
}
