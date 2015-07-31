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

import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.property.Property;
import org.qi4j.lib.swing.binding.Binding;
import org.qi4j.lib.swing.binding.SwingAdapter;

public final class BoundProperty<T> extends AbstractBinding<T>
    implements Property<T>, Binding
{

    private Property propertyInfo;

    /**
     * Construct an instance of {@code BoundProperty} with the specified arguments.
     *
     * @param propertyMethod The method that returns Property. This argument must not be {@code null}.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aMethod} is {@code null}.
     */
    public BoundProperty( @Uses Method propertyMethod, @Structure ObjectFactory objectBuilderFactory,
                          @Service Iterable<SwingAdapter> allAdapters
    )
        throws IllegalArgumentException
    {
        super( propertyMethod, objectBuilderFactory, allAdapters );
    }

    public void use( Property<T> actualProperty )
    {
        T value = null;
        if( actualProperty != null )
        {
            value = actualProperty.get();
        }
        stateModel.use( value );
        for( JComponent component : components )
        {
            SwingAdapter adapter = adapters.get( component.getClass() );
            adapter.fromPropertyToSwing( component, actualProperty );
            for( FocusListener listener : component.getFocusListeners() )
            {
                if( PropertyFocusLostListener.class.isInstance( listener ) )
                {
                    ( (PropertyFocusLostListener) listener ).use( adapter, actualProperty );
                }
            }
        }
    }

    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return capabilities.property;
    }
}
