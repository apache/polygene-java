/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.lib.swing.binding.internal;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.lib.swing.binding.SwingBinding;

public abstract class AbstractBinding<T>
    implements SwingBinding
{

    private final Class<T> type;
    protected StateModel<T> stateModel;
    protected Set<JComponent> components;
    private PropertyFocusLostListener focusListener;
    protected HashMap<Class<? extends JComponent>, SwingAdapter> adapters;
    private String name;
    protected Method method;

    @SuppressWarnings( "unchecked" )
    public AbstractBinding( @Uses Method method, @Structure ObjectBuilderFactory objectBuilderFactory,
                            @Service Iterable<SwingAdapter> allAdapters )
        throws IllegalArgumentException
    {
        this.method = method;
        ParameterizedType parameterType = (ParameterizedType) method.getGenericReturnType();
        Type[] actualTypeArguments = parameterType.getActualTypeArguments();
        type = (Class<T>) actualTypeArguments[ 0 ];
        name = method.getName();
        ObjectBuilder<StateModel> builder = objectBuilderFactory.newObjectBuilder( StateModel.class );
        builder.use( type );
        stateModel = builder.newInstance();
        components = new HashSet<JComponent>();
        setupAdapters( allAdapters );
    }

    public void to( final JComponent component )
    {
        if( components.add( component ) )
        {
            focusListener = new PropertyFocusLostListener( component );
            component.addFocusListener( focusListener );
        }
    }

    public String toString()
    {
        return name + "[" + type.getSimpleName() + "] -> " + stateModel.toString();
    }

    private void setupAdapters( Iterable<SwingAdapter> allAdapters )
    {
        adapters = new HashMap<Class<? extends JComponent>, SwingAdapter>();
        for( SwingAdapter adapterCandidate : allAdapters )
        {
            Set<SwingAdapter.Capabilities> canHandle = adapterCandidate.canHandle();
            for( SwingAdapter.Capabilities capabilities : canHandle )
            {
                if( requiredCapabilitySatisfied( capabilities ) )
                {
                    if( capabilities.type.equals( this.type ) )
                    {
                        Class<? extends JComponent> component = capabilities.component;
                        adapters.put( component, adapterCandidate );
                    }
                }
            }
        }
    }

    protected abstract boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities );

    public final T get()
    {
        return stateModel.state();
    }

    public final Class type()
    {
        return type;
    }

    public void set( T newValue )
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException( "set() is not allowed in binding templates." );
    }

}
