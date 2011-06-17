/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.composite;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModelModule;
import org.qi4j.spi.composite.CompositeInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * JAVADOC
 */
public final class TransientBuilderInstance<T>
    implements TransientBuilder<T>
{
    private static final Method TYPE_METHOD;
    private static final Method METAINFO_METHOD;

    static
    {
        try
        {
            TYPE_METHOD = Composite.class.getMethod( "type" );
            METAINFO_METHOD = Composite.class.getMethod( "metaInfo", Class.class );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: TransientBuilderInstance" );
        }
    }

    private ModelModule<TransientModel> model;

    // lazy initialized in accessor
    private UsesInstance uses = UsesInstance.EMPTY_USES;

    // lazy initialized in accessor
    private CompositeInstance prototypeInstance;

    // lazy initialized in accessor
    private StateHolder state;

    public TransientBuilderInstance( ModelModule<TransientModel> model, UsesInstance uses )
    {
        this( model );
        this.uses = uses;
    }

    public TransientBuilderInstance( ModelModule<TransientModel> model )
    {
        this.model = model;
    }

    public Class<T> compositeType()
    {
        return (Class<T>) model.model().type();
    }

    public TransientBuilder<T> use( Object... usedObjects )
    {
        uses = uses.use( usedObjects );

        return this;
    }

    public T prototype()
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = model.model().newCompositeInstance( model.module(), uses, getState() );
        }

        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = model.model().newCompositeInstance( model.module(), uses, getState() );
        }

        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws ConstructionException
    {
        StateHolder instanceState;
        if( state == null )
        {
            instanceState = model.model().newInitialState();
        }
        else
        {
            instanceState = model.model().newState( state );
        }

        model.model().state().checkConstraints( instanceState );

        CompositeInstance compositeInstance =
            model.model().newCompositeInstance( model.module(), uses, instanceState );
        return compositeInstance.<T>proxy();
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StateHolder getState()
    {
        if( state == null )
        {
            state = model.model().newBuilderState();
        }

        return state;
    }

    protected class StateInvocationHandler
        implements InvocationHandler
    {
        public StateInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return getState().getProperty( method );
            }
            else if( method.equals( TYPE_METHOD ) )
            {
                return model.model().type();
            }
            else if( method.equals( METAINFO_METHOD ) )
            {
                return model.model().metaInfo( (Class<? extends Object>) objects[ 0 ] );
            }
            else
            {
                throw new IllegalArgumentException( "Method does not represent state: " + method );
            }
        }
    }
}
