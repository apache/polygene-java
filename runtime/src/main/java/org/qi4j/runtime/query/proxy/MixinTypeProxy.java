/*
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import static java.lang.reflect.Proxy.newProxyInstance;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.runtime.query.QueryException;

/**
 * TODO Add JavaDoc
 */
public final class MixinTypeProxy
    implements InvocationHandler
{

    /**
     * Class of template this proxy is for.
     */
    private final Class templateClass;
    /**
     * Traversed association.
     */
    private final AssociationReference traversedAssociation;

    /**
     * Constructor.
     *
     * @param templateClass class of template this proxy is for
     */
    public MixinTypeProxy( final Class templateClass )
    {
        this( templateClass, null );
    }

    /**
     * Constructor.
     *
     * @param templateClass        class of template this proxy is for
     * @param traversedAssociation traversed association
     */
    public MixinTypeProxy( final Class templateClass,
                           final AssociationReference traversedAssociation )
    {
        this.templateClass = templateClass;
        this.traversedAssociation = traversedAssociation;
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args )
    {
        if( args == null )
        {
            Class<?> methodReturnType = method.getReturnType();
            if( Property.class.isAssignableFrom( methodReturnType ) )
            {
                return newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ methodReturnType, PropertyReference.class },
                    new PropertyReferenceProxy( method, traversedAssociation )
                );
            }
            else if( Association.class.isAssignableFrom( methodReturnType ) )
            {
                return newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ methodReturnType, AssociationReference.class },
                    new AssociationReferenceProxy( method, traversedAssociation )
                );
            }
            else if( ManyAssociation.class.isAssignableFrom( methodReturnType ) )
            {
                return newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ methodReturnType, AssociationReference.class },
                    new ManyAssociationReferenceProxy( method, traversedAssociation )
                );
            }
        }

        throw new QueryException(
            "Only property, association and many associations methods can be used" );
    }

    @Override
    public final String toString()
    {
        return "Template for " + templateClass.getName();
    }
}