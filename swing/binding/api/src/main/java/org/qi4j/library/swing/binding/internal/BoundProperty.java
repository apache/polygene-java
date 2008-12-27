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
package org.qi4j.library.swing.binding.internal;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.binding.IllegalBindingException;
import org.qi4j.library.swing.binding.StateModel;
import org.qi4j.library.swing.binding.SwingAdapter;

public final class BoundProperty<T> extends AbstractBinding<T, T, Property<T>>
    implements Property<T>
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
    protected final boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
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

    public final void set( T newValue )
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException( "set() is not allowed in binding templates." );
    }

    public final <K> K metaInfo( Class<K> infoType )
    {
        return propertyInfo().metaInfo( infoType );
    }

    private PropertyInfo propertyInfo()
    {
        if( propertyInfo == null )
        {
            propertyInfo = new GenericPropertyInfo( fieldMethod );
        }
        return propertyInfo;
    }

    public final String name()
    {
        return propertyInfo().name();
    }

    public final String qualifiedName()
    {
        return propertyInfo().qualifiedName();
    }

    public boolean isImmutable()
    {
        return propertyInfo().isImmutable();
    }

    public boolean isComputed()
    {
        return propertyInfo().isComputed();
    }

    @SuppressWarnings( "unchecked" )
    public final void stateToUse( T aNewStateInUse )
    {
        // Update components
        Set<Map.Entry<JComponent, FocusLostListener>> entries = components.entrySet();
        for( Map.Entry<JComponent, FocusLostListener> entry : entries )
        {
            JComponent component = entry.getKey();

            Class<? extends JComponent> componentClass = component.getClass();
            SwingAdapter adapter = adapters.get( componentClass );
            adapter.fromDataToSwing( component, actualProperty );

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

    public final void fieldToUse( Property<T> anActualProperty )
    {
        actualProperty = anActualProperty;

        T propertyValue = null;
        if( anActualProperty != null )
        {
            propertyValue = anActualProperty.get();
        }
        stateModel.use( propertyValue );
    }

    public <C extends JComponent> C to( C aComponent )
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
            adapter.fromDataToSwing( aComponent, actualProperty );

            aComponent.addFocusListener( focusListener );
            components.put( aComponent, focusListener );
        }

        return aComponent;
    }

    private class FocusLostListener
        implements FocusListener
    {
        private SwingAdapter adapter;

        private void setAdapter( SwingAdapter anAdapterToUse )
        {
            adapter = anAdapterToUse;
        }

        public final void focusGained( FocusEvent e )
        {
            // Do nothing
        }

        public final void focusLost( FocusEvent e )
        {
            if( adapter != null && actualProperty != null )
            {
                JComponent component = (JComponent) e.getComponent();

                adapter.fromSwingToData( component, actualProperty );

                stateToUse( actualProperty.get() );
            }
        }
    }
}
