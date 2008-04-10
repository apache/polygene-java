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
package org.qi4j.query.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.qi4j.entity.association.Association;
import org.qi4j.property.Property;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.query.grammar.PropertyReference;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public class MixinTypeProxy
    implements InvocationHandler
{

    /**
     * Class of template this proxy is for.
     */
    private Class templateClass;
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
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[]{ method.getReturnType(), PropertyReference.class },
                    new PropertyReferenceProxy( method, traversedAssociation )
                );
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[]{ method.getReturnType(), AssociationReference.class },
                    new AssociationReferenceProxy( method, traversedAssociation )
                );
            }
        }
        throw new UnsupportedOperationException( "Only property and association methods can be used" );
    }

    @Override public String toString()
    {
        return "Template for " + templateClass.getName();
    }
}