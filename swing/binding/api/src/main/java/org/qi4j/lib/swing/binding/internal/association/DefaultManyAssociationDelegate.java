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
package org.qi4j.lib.swing.binding.internal.association;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.lib.swing.binding.SwingBinding;

public abstract class DefaultManyAssociationDelegate<T, K extends ManyAssociation<T>>
    implements ManyAssociationDelegate<T, K>
{

    private K manyAssociation;

    private Map<Class<? extends JComponent>, SwingAdapter> adapters;

    private WeakHashMap<JComponent, FocusLostListener> components;

    @SuppressWarnings( "unchecked" )
    public DefaultManyAssociationDelegate(
        @Uses Map<Class<? extends JComponent>, SwingAdapter> adapters )

        throws IllegalArgumentException
    {
        this.adapters = adapters;

        components = new WeakHashMap<JComponent, FocusLostListener>();
    }

    public final StateModel<T> stateModel()
    {
        return null;
    }

    public final SwingBinding<T> to( JComponent aComponent )
    {
        validateNotNull( "aComponent", aComponent );

        if( !components.containsKey( aComponent ) )
        {
            FocusLostListener focusListener = new FocusLostListener();

            // Sets the adapter for focus listener to use
            Class<? extends JComponent> componentClass = aComponent.getClass();
            SwingAdapter adapter = adapters.get( componentClass );
            focusListener.setAdapter( adapter );

            // Initialized component initial value
            handleFromManyAssociationToSwing( adapter, aComponent, manyAssociation );

            aComponent.addFocusListener( focusListener );
            components.put( aComponent, focusListener );
        }

        return this;
    }

    protected abstract void handleFromManyAssociationToSwing( SwingAdapter adapter, JComponent component,
                                                              K manyAssociation );

    protected abstract void handleFromSwingToManyAssociation( SwingAdapter adapter, JComponent component,
                                                              K manyAssociation );

    public void stateToUse( K aNewState )
    {
        // Update components
        for( Map.Entry<JComponent, FocusLostListener> entry : components.entrySet() )
        {
            JComponent component = entry.getKey();

            Class<? extends JComponent> componentClass = component.getClass();
            SwingAdapter adapter = adapters.get( componentClass );
            handleFromManyAssociationToSwing( adapter, component, manyAssociation );

            FocusLostListener focusListener = entry.getValue();
            focusListener.setAdapter( adapter );
        }
    }

    public void fieldToUse( K anActualField )
    {
        manyAssociation = anActualField;
        stateToUse( anActualField );
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
        }

        public void focusLost( FocusEvent e )
        {
            if( adapter != null && manyAssociation != null )
            {
                JComponent component = (JComponent) e.getComponent();
                handleFromSwingToManyAssociation( adapter, component, manyAssociation );

                stateToUse( manyAssociation );
            }
        }
    }
}
