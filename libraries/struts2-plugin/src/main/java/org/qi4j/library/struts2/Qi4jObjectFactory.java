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
package org.qi4j.library.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.apache.struts2.util.ObjectFactoryDestroyable;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.NoSuchTransientException;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.object.ObjectFactory;

import static org.qi4j.library.struts2.Qi4jObjectFactory.ClassType.*;

/**
 * Qi4j implementation of struts object factory.
 */
public class Qi4jObjectFactory
    extends com.opensymphony.xwork2.ObjectFactory
    implements ObjectFactoryDestroyable
{
    private static final long serialVersionUID = 1L;

    static enum ClassType
    {
        qi4jComposite,
        qi4jObject,
        object
    }

    private final Map<Class, ClassType> types;

    private ObjectFactory objectFactory;
    private TransientBuilderFactory compositeBuilderFactory;

    public Qi4jObjectFactory()
    {
        types = new HashMap<Class, ClassType>();
    }

    @Inject
    public void setObjectFactory( ObjectFactory objectFactory )
    {
        this.objectFactory = objectFactory;
    }

    @Inject
    public void setCompositeBuilderFactory( TransientBuilderFactory compositeBuilderFactory )
    {
        this.compositeBuilderFactory = compositeBuilderFactory;
    }

    /**
     * Build a generic Java object of the given type.
     *
     * @param classType    Type of Object to build
     * @param extraContext A map of extra context which uses the same keys as the {@link ActionContext}
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public Object buildBean( Class classType, Map extraContext )
        throws Exception
    {
        // TODO: What to do with extraContext

        ClassType type = types.get( classType );
        if( type != null )
        {
            switch( type )
            {
            case object:
                return createStandardObject( classType, false );
            case qi4jComposite:
                return createQi4jComposite( classType, false );
            case qi4jObject:
                return createQi4jObject( classType, false );
            }
        }

        // Figure out what kind of object is this.
        Object object = createQi4jComposite( classType, true );
        if( object == null )
        {
            object = createQi4jObject( classType, true );

            if( object == null )
            {
                object = createStandardObject( classType, true );
            }
        }

        return object;
    }

    private Object createStandardObject( Class aClass, boolean isAddToTypes )
        throws Exception
    {
        Object obj = null;
        Exception exception = null;
        try
        {
            obj = aClass.newInstance();
        }
        catch( InstantiationException e )
        {
            exception = e;
        }
        catch( IllegalAccessException e )
        {
            exception = e;
        }

        if( isAddToTypes )
        {
            addToType( aClass, object );
        }

        if( exception != null )
        {
            throw exception;
        }

        return obj;
    }

    @SuppressWarnings( "unchecked" )
    private Object createQi4jObject( Class aClass, boolean isAddToTypes )
    {
        if( objectFactory == null )
        {
            return null;
        }

        ConstructionException exception = null;
        Object obj = null;

        try
        {
            obj = objectFactory.newObject( aClass );
        }
        catch( NoSuchObjectException e )
        {
            return null;
        }
        catch( ConstructionException e )
        {
            exception = e;
        }

        if( isAddToTypes )
        {
            addToType( aClass, qi4jObject );
        }

        if( exception != null )
        {
            throw exception;
        }

        return obj;
    }

    @SuppressWarnings( "unchecked" )
    private Object createQi4jComposite( Class aClass, boolean isAddToTypes )
    {
        if( compositeBuilderFactory == null )
        {
            return null;
        }

        Object obj = null;
        ConstructionException exception = null;

        try
        {
            obj = compositeBuilderFactory.newTransient( aClass );
        }
        catch( NoSuchTransientException e )
        {
            return null;
        }
        catch( ConstructionException e )
        {
            exception = e;
        }

        if( isAddToTypes )
        {
            addToType( aClass, qi4jComposite );
        }

        if( exception != null )
        {
            throw exception;
        }

        return obj;
    }

    private void addToType( Class aClass, ClassType aClassType )
    {
        synchronized( types )
        {
            types.put( aClass, aClassType );
        }
    }

    /**
     * Allows for ObjectFactory implementations that support Actions without no-arg constructors.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isNoArgConstructorRequired()
    {
        return false;
    }

    @Override
    public final void destroy()
    {
        types.clear();
    }
}
