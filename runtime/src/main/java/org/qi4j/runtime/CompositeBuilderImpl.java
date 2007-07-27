/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Method;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeBuilder;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.CompositeModelFactory;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.model.CompositeState;
import org.qi4j.api.persistence.Lifecycle;
import org.qi4j.api.persistence.impl.LifecycleImpl;

/**
 *
 */
public class CompositeBuilderImpl<T extends Composite> extends MixinBuilderImpl<T>
    implements CompositeBuilder<T>
{
    private static final Method CREATE_METHOD;

    static
    {
        try
        {
            CREATE_METHOD = Lifecycle.class.getMethod( "create" );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Lifecycle class is corrupt." );
        }
    }

    CompositeBuilderImpl( FragmentFactory fragmentFactory, CompositeModelFactory modelFactory, CompositeBuilderFactoryImpl builderFactory, Class<T> compositeInterface )
    {
        super( fragmentFactory, modelFactory, builderFactory, compositeInterface );
        states.put( Lifecycle.class, new LifecycleImpl() );
    }

    public T newInstance()
    {
        T composite = builderFactory.newInstance( compositeInterface );
        CompositeState state = RegularCompositeInvocationHandler.getInvocationHandler( composite );
        state.setMixins( states, false );
        try
        {
            ( (RegularCompositeInvocationHandler) state ).invoke( composite, CREATE_METHOD, null );
        }
        catch( InvocationTargetException e )
        {
            Throwable t = e.getTargetException();
            throw new CompositeInstantiationException( t );
        }
        catch( UndeclaredThrowableException e )
        {
            Throwable t = e.getUndeclaredThrowable();
            throw new CompositeInstantiationException( t );
        }
        catch( RuntimeException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new CompositeInstantiationException( e );
        }
        return composite;
    }
}
