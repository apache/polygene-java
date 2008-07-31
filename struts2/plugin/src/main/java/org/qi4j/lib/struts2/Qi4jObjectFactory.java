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
package org.qi4j.lib.struts2;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.util.ObjectFactoryDestroyable;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ConstructionException;
import org.qi4j.composite.NoSuchCompositeException;
import org.qi4j.injection.scope.Structure;
import static org.qi4j.lib.struts2.Constants.SERVLET_ATTRIBUTE;
import static org.qi4j.lib.struts2.Qi4jObjectFactory.ClassType.object;
import static org.qi4j.lib.struts2.Qi4jObjectFactory.ClassType.qi4jComposite;
import static org.qi4j.lib.struts2.Qi4jObjectFactory.ClassType.qi4jObject;
import org.qi4j.object.NoSuchObjectException;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.structure.Module;

/**
 * Qi4j implementation of struts object factory.
 *
 * @author edward.yakop@gmail.com
 */
public class Qi4jObjectFactory extends ObjectFactory
    implements ObjectFactoryDestroyable
{
    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog( Qi4jObjectFactory.class );

    static enum ClassType
    {
        qi4jComposite,
        qi4jObject,
        object
    }

    @Structure
    private Module module;

    private final Map<Class, ClassType> types;

    public Qi4jObjectFactory()
    {
        types = new HashMap<Class, ClassType>();
    }

    @Inject
    public void setServletContext( ServletContext aServletContext )
    {
        LOG.info( "Qi4j Plugin: Initializing" );

        Object appInitializer = aServletContext.getAttribute( SERVLET_ATTRIBUTE );
        if( appInitializer instanceof RuntimeException )
        {
            throw (RuntimeException) appInitializer;
        }
        if( appInitializer instanceof Error )
        {
            throw (Error) appInitializer;
        }
        if( !( appInitializer instanceof Module ) )
        {
            throw new IllegalStateException( "No Qi4jStrutsApplicationBootstrap found:" );
        }

        module = (Module) appInitializer;

        LOG.info( "... initialized qi4j-struts integration successfully" );
    }

    /**
     * Build a generic Java object of the given type.
     *
     * @param aClass       Type of Object to build
     * @param extraContext A map of extra context which uses the same keys as the {@link ActionContext}
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public Object buildBean( Class aClass, Map extraContext )
        throws Exception
    {
        // TODO: What to do with extraContext

        ClassType type = types.get( aClass );
        if( type != null )
        {
            switch( type )
            {
            case object:
                return createStandardObject( aClass, false );
            case qi4jComposite:
                return createQi4jComposite( aClass, false );
            case qi4jObject:
                return createQi4jObject( aClass, false );
            }
        }

        // Figure out what kind of object is this.
        Object object = createQi4jComposite( aClass, true );
        if( object == null )
        {
            object = createQi4jObject( aClass, true );

            if( object == null )
            {
                object = createStandardObject( aClass, true );
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
        ConstructionException exception = null;
        ObjectBuilderFactory builderFactory = module.objectBuilderFactory();
        Object obj = null;

        try
        {
            obj = builderFactory.newObject( aClass );
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
        Object obj = null;
        ConstructionException exception = null;
        CompositeBuilderFactory cbf = module.compositeBuilderFactory();
        try
        {
            obj = cbf.newComposite( aClass );
        }
        catch( NoSuchCompositeException e )
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

    public final void destroy()
    {
        types.clear();
        module = null;
    }
}
