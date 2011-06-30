/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.lib.swing.binding.internal;

import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.property.Property;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author edward.yakop@gmail.com
 */
public final class StateInvocationHandler<T>
    implements InvocationHandler
{

    @Structure
    private ObjectBuilderFactory objectBuilderFactory;
    private final HashMap<Method, BoundProperty> properties;
    private final HashMap<Method, BoundAssociation> associations;
    private final HashMap<Method, BoundSetAssociation> setAssociations;
    private final HashMap<Method, BoundListAssociation> listAssociations;
    private final Class type;

    public StateInvocationHandler( @Uses Class aType )
    {
        type = aType;
        properties = new HashMap<Method, BoundProperty>();
        associations = new HashMap<Method, BoundAssociation>();
        setAssociations = new HashMap<Method, BoundSetAssociation>();
        listAssociations = new HashMap<Method, BoundListAssociation>();
    }

    public final Object invoke( Object aProxy, Method aMethod, Object[] args )
        throws Throwable
    {
        if( "toString".equals( aMethod.getName() ) )
        {
            return type.toString() + "( " + properties.values() + ")";
        }
        Class<?> methodReturnType = aMethod.getReturnType();
        if( Property.class.isAssignableFrom( methodReturnType ) )
        {
            BoundProperty property = properties.get( aMethod );
            if( property == null )
            {
                ObjectBuilder<BoundProperty> objectBuilder =
                    objectBuilderFactory.newObjectBuilder( BoundProperty.class );
                objectBuilder.use( aMethod );
                property = objectBuilder.newInstance();
                properties.put( aMethod, property );
            }

            return property;
        }
        else if( ListAssociation.class.isAssignableFrom( methodReturnType ) )
        {
            BoundListAssociation association = listAssociations.get( aMethod );
            if( association == null )
            {
                ObjectBuilder<BoundListAssociation> objectBuilder =
                    objectBuilderFactory.newObjectBuilder( BoundListAssociation.class );
                objectBuilder.use( aMethod );
                association = objectBuilder.newInstance();
                listAssociations.put( aMethod, association );
            }
            return association;
        }
        else if( SetAssociation.class.isAssignableFrom( methodReturnType ) )
        {
            BoundSetAssociation association = setAssociations.get( aMethod );
            if( association == null )
            {
                ObjectBuilder<BoundSetAssociation> objectBuilder =
                    objectBuilderFactory.newObjectBuilder( BoundSetAssociation.class );
                objectBuilder.use( aMethod );
                association = objectBuilder.newInstance();
                setAssociations.put( aMethod, association );
            }
            return association;
        }
        else if( Association.class.isAssignableFrom( methodReturnType ) )
        {
            BoundAssociation association = associations.get( aMethod );
            if( association == null )
            {
                ObjectBuilder<BoundAssociation> objectBuilder =
                    objectBuilderFactory.newObjectBuilder( BoundAssociation.class );
                objectBuilder.use( aMethod );
                association = objectBuilder.newInstance();
                associations.put( aMethod, association );
            }
            return association;
        }
        return null;
    }

    public void use( T actualData )
    {
        for( Map.Entry<Method, BoundProperty> entry : properties.entrySet() )
        {
            BoundProperty bound = entry.getValue();
            Property actualProperty = null;
            if( actualData != null )
            {
                Method method = entry.getKey();
                try
                {
                    actualProperty = (Property) method.invoke( actualData );
                } catch( IllegalAccessException e )
                {
                    e.printStackTrace();  //TODO: Auto-generated, need attention.
                } catch( InvocationTargetException e )
                {
                    e.printStackTrace();  //TODO: Auto-generated, need attention.
                }
            }
            bound.use( actualProperty );
        }

    }
}

