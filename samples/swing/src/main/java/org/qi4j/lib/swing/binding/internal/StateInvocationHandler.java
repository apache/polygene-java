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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

public final class StateInvocationHandler<T>
    implements InvocationHandler
{

    @Structure
    private Module module;

    private final HashMap<Method, BoundProperty> properties;
    private final HashMap<Method, BoundAssociation> associations;
    private final HashMap<Method, BoundManyAssociation> manyassociations;
    private final HashMap<Method, BoundNamedAssociation> namedassociations;
    private final Class<?> type;

    public StateInvocationHandler( @Uses Class<?> aType )
    {
        type = aType;
        properties = new HashMap<>();
        associations = new HashMap<>();
        manyassociations = new HashMap<>();
        namedassociations = new HashMap<>();
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
                property = module.newObject( BoundProperty.class, aMethod );
                properties.put( aMethod, property );
            }

            return property;
        }
        else if( NamedAssociation.class.isAssignableFrom( methodReturnType ) )
        {
            BoundNamedAssociation association = namedassociations.get( aMethod );
            if( association == null )
            {
                association = module.newObject( BoundNamedAssociation.class, aMethod );
                namedassociations.put( aMethod, association );
            }
            return association;
        }
        else if( ManyAssociation.class.isAssignableFrom( methodReturnType ) )
        {
            BoundManyAssociation association = manyassociations.get( aMethod );
            if( association == null )
            {
                association = module.newObject( BoundManyAssociation.class, aMethod );
                manyassociations.put( aMethod, association );
            }
            return association;
        }
        else if( Association.class.isAssignableFrom( methodReturnType ) )
        {
            BoundAssociation association = associations.get( aMethod );
            if( association == null )
            {
                association = module.newObject( BoundAssociation.class, aMethod );
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
                }
                catch( IllegalAccessException | InvocationTargetException e )
                {
                    e.printStackTrace();  //TODO: Auto-generated, need attention.
                }
            }
            bound.use( actualProperty );
        }
    }
}
