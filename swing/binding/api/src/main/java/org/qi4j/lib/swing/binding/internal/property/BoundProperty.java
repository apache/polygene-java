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
package org.qi4j.lib.swing.binding.internal.property;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.IllegalBindingException;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.lib.swing.binding.SwingBinding;
import org.qi4j.lib.swing.binding.internal.AbstractBinding;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;

public final class BoundProperty<T> extends AbstractBinding<T, T, Property<T>>
    implements ImmutableProperty<T>
{

    private Property<T> actualProperty;
    private PropertyInfo propertyInfo;

    private WeakHashMap<JComponent, FocusLostListener> components;

    /**
     * Construct an instance of {@code BoundProperty} with the specified arguments.
     *
     * @param aPropertyMethod The method that returns Property. This argument must not be {@code null}.
     * @param anOBF           The object builder factory. This argument must not be {@code null}.
     * @param allAdapters     All adapters. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or all arguments are {@code null}.
     */
    @SuppressWarnings( "unchecked" )
    public BoundProperty(
        @Uses Method aPropertyMethod,
        @Structure ObjectBuilderFactory anOBF,
        @Service Iterable<SwingAdapter> allAdapters )
        throws IllegalArgumentException
    {
        super( aPropertyMethod, anOBF, allAdapters );

        components = new WeakHashMap<JComponent, FocusLostListener>();
    }

    @Override
    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return capabilities.property;
    }

    public final StateModel<T> stateModel()
    {
        return stateModel;
    }

    public final Method propertyMethod()
    {
        return fieldMethod;
    }

    @SuppressWarnings( "unchecked" )
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

    public <K> K metaInfo( Class<K> infoType )
    {
        if( propertyInfo == null )
        {
            createPropertyInfo();
        }
        return propertyInfo.metaInfo( infoType );
    }

    public String name()
    {
        if( propertyInfo == null )
        {
            createPropertyInfo();
        }
        return propertyInfo.name();
    }

    public String qualifiedName()
    {
        if( propertyInfo == null )
        {
            createPropertyInfo();
        }
        return propertyInfo.qualifiedName();
    }

    private void createPropertyInfo()
    {
        propertyInfo = new GenericPropertyInfo( this.fieldMethod );
    }

    @Override
    public final String toString()
    {
        return name + "[" + type.getSimpleName() + "] -> " + stateModel.toString();
    }

    @SuppressWarnings( "unchecked" )
    public final void stateInUse( T aNewStateInUse )
    {
        // Update components
        for( Map.Entry<JComponent, FocusLostListener> entry : components.entrySet() )
        {
            JComponent component = entry.getKey();

            Class<? extends JComponent> componentClass = component.getClass();
            SwingAdapter adapter = adapters.get( componentClass );
            adapter.fromPropertyToSwing( component, actualProperty );

            FocusLostListener focusListener = entry.getValue();
            focusListener.setAdapter( adapter );
        }

        if( actualProperty == null ||
            actualProperty.get() == aNewStateInUse )
        {
            return;
        }

        if( actualProperty != null )
        {
            actualProperty.set( aNewStateInUse );
        }

        stateModel.use( aNewStateInUse );
    }

    public final void fieldInUse( Property<T> anActualProperty )
    {
        actualProperty = anActualProperty;

        T propertyValue = null;
        if( anActualProperty != null )
        {
            propertyValue = anActualProperty.get();
        }
        stateModel.use( propertyValue );
    }

    public final SwingBinding<T> to( JComponent aComponent )
        throws IllegalBindingException
    {
        validateNotNull( "aComponent", aComponent );

        if( !components.containsKey( aComponent ) )
        {
            FocusLostListener focusListener = new FocusLostListener();

            // Sets the adapter for focus listener to use
            Class<? extends JComponent> componentClass = aComponent.getClass();
            SwingAdapter adapter = adapters.get( componentClass );

            if( adapter == null )
            {
                throw new IllegalBindingException( aComponent, type );
            }
            focusListener.setAdapter( adapter );

            // Initialized component initial value
            adapter.fromPropertyToSwing( aComponent, actualProperty );

            aComponent.addFocusListener( focusListener );
            components.put( aComponent, focusListener );
        }

        return this;
    }

    private class FocusLostListener
        implements FocusListener
    {

        private SwingAdapter adapter;

        private void setAdapter( SwingAdapter adapterToUse )
        {
            adapter = adapterToUse;
        }

        public final void focusGained( FocusEvent e )
        {
            // Do nothing
        }

        public void focusLost( FocusEvent e )
        {
            if( adapter != null && actualProperty != null )
            {
                JComponent component = (JComponent) e.getComponent();

                adapter.fromSwingToProperty( component, actualProperty );

                stateInUse( actualProperty.get() );
            }
        }
    }
}
