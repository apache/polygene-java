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
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.ManyAssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;

import static java.lang.reflect.Proxy.*;

/**
 * JAVADOC Add JavaDoc
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
     * Traversed property.
     */
    private final PropertyReference traversedProperty;

    /**
     * Constructor.
     *
     * @param templateClass class of template this proxy is for
     */
    public MixinTypeProxy( final Class templateClass )
    {
        this( templateClass, null, null );
    }

    /**
     * Constructor.
     *
     * @param templateClass        class of template this proxy is for
     * @param traversedAssociation traversed association
     */
    public MixinTypeProxy( final Class templateClass,
                           final AssociationReference traversedAssociation
    )
    {
        this( templateClass, traversedAssociation, null );
    }

    /**
     * Constructor.
     *
     * @param templateClass     class of template this proxy is for
     * @param traversedProperty traversed property
     */
    public MixinTypeProxy( final Class templateClass,
                           final PropertyReference traversedProperty
    )
    {
        this( templateClass, null, traversedProperty );
    }

    /**
     * Constructor.
     *
     * @param templateClass        class of template this proxy is for
     * @param traversedAssociation traversed association
     * @param traversedProperty    traversed property
     */
    private MixinTypeProxy( final Class templateClass,
                            final AssociationReference traversedAssociation,
                            final PropertyReference traversedProperty
    )
    {
        this.templateClass = templateClass;
        this.traversedAssociation = traversedAssociation;
        this.traversedProperty = traversedProperty;
    }

    public AssociationReference traversedAssociation()
    {
        return traversedAssociation;
    }

    public PropertyReference traversedProperty()
    {
        return traversedProperty;
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args
    )
    {
        if( args == null )
        {
            Class<?> methodReturnType = method.getReturnType();
            if( Property.class.isAssignableFrom( methodReturnType ) )
            {
                return newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ methodReturnType, PropertyReference.class },
                    new PropertyReferenceProxy( method, traversedAssociation, traversedProperty )
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
                    new Class[]{ methodReturnType, ManyAssociationReference.class },
                    new ManyAssociationReferenceProxy( method, traversedAssociation )
                );
            }
        }

        throw new NotQueryableException(
            "Only property, association and many manyAssociations methods can be used" );
    }

    @Override
    public final String toString()
    {
        return "Template for " + templateClass.getName();
    }
}