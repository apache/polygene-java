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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.entity.association.SetAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.lib.swing.binding.internal.association.BoundAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundListAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundSetAssociation;
import org.qi4j.lib.swing.binding.internal.property.BoundProperty;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;

/**
 * @author edward.yakop@gmail.com
 */
public final class StateInvocationHandler<T>
    implements InvocationHandler
{

    @Structure private ObjectBuilderFactory obf;

    private final HashMap<Method, BoundField> fields;
    private final Class type;
    private T actualData;

    public StateInvocationHandler( @Uses Class aType )
    {
        type = aType;
        fields = new HashMap<Method, BoundField>();
    }

    @SuppressWarnings( "unchecked" )
    public final Object invoke( Object aProxy, Method aMethod, Object[] args )
        throws Throwable
    {
        if( "toString".equals( aMethod.getName() ) )
        {
            return type.toString() + "( " + fields.values() + ")";
        }
        Class<?> methodReturnType = aMethod.getReturnType();

        if( Property.class.isAssignableFrom( methodReturnType ) ||
            Association.class.isAssignableFrom( methodReturnType ) ||
            ManyAssociation.class.isAssignableFrom( methodReturnType ) )
        {
            BoundField field = fields.get( aMethod );
            if( field == null )
            {
                ObjectBuilder objectBuilder;

                if( Property.class.isAssignableFrom( methodReturnType ) )
                {
                    objectBuilder = obf.newObjectBuilder( BoundProperty.class );
                }
                else if( SetAssociation.class.isAssignableFrom( methodReturnType ) )
                {
                    objectBuilder = obf.newObjectBuilder( BoundSetAssociation.class );
                }
                else if( ListAssociation.class.isAssignableFrom( methodReturnType ) )
                {
                    objectBuilder = obf.newObjectBuilder( BoundListAssociation.class );
                }
                else if( Association.class.isAssignableFrom( methodReturnType ) )
                {
                    objectBuilder = obf.newObjectBuilder( BoundAssociation.class );
                }
                else
                {
                    String message = "Field type [" + methodReturnType + "] is not supported.";
                    throw new UnsupportedOperationException( message );
                }

                objectBuilder.use( aMethod );
                field = (BoundField) objectBuilder.newInstance();

                // Initialized value if possible
                Object actualField = actualField( actualData, aMethod );
                field.fieldToUse( actualField );

                fields.put( aMethod, field );
            }

            return field;
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    public void use( T anActualData )
    {
        actualData = anActualData;

        for( Map.Entry<Method, BoundField> entry : fields.entrySet() )
        {
            Method methodToActualField = entry.getKey();

            BoundField field = entry.getValue();
            Object actualField = actualField( anActualData, methodToActualField );
            field.fieldToUse( actualField );
        }
    }

    private Object actualField( T anActualData, Method aMethodToActualField )
    {
        Object actualField = null;
        if( anActualData != null )
        {
            try
            {
                actualField = aMethodToActualField.invoke( anActualData );
            }
            catch( IllegalAccessException e )
            {
                // Shouldn't happened.
                e.printStackTrace();
            }
            catch( InvocationTargetException e )
            {
                // Shouldn't happened.
                e.printStackTrace();  //TODO: Auto-generated, need attention.
            }
        }

        return actualField;
    }
}

