package org.qi4j.lib.swing.binding.internal;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.lib.swing.binding.IllegalBindingException;
import org.qi4j.lib.swing.binding.StateModel;
import org.qi4j.lib.swing.binding.SwingAdapter;

/**
 * @author Lan Boon Ping
 */
public final class BoundAssociation<T> extends AbstractBinding<T, T, Association<T>>
    implements Association<T>
{
    private Association<T> actualAssociation;
    private WeakHashMap<JComponent, FocusLostListener> components;

    /**
     * Construct an instance of {@code BoundAssociation} with the specified arguments.
     *
     * @param aPropertyMethod The method that returns Property. This argument must not be {@code null}.
     * @param anOBF           The object builder factory. This argument must not be {@code null}.
     * @param allAdapters     All adapters. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if one or all arguments are {@code null}.
     */
    @SuppressWarnings( "unchecked" )
    public BoundAssociation(
        @Uses Method aPropertyMethod,
        @Structure ObjectBuilderFactory anOBF,
        @Service Iterable<SwingAdapter> allAdapters )
        throws IllegalArgumentException
    {
        super( aPropertyMethod, anOBF, allAdapters );

        components = new WeakHashMap<JComponent, FocusLostListener>();
    }

    public final StateModel<T> stateModel()
    {
        return stateModel;
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
        if( actualAssociation == null )
        {
            return null;
        }
        return actualAssociation.metaInfo( infoType );
    }

    public String name()
    {
        if( actualAssociation == null )
        {
            return null;
        }
        return actualAssociation.name();
    }

    public String qualifiedName()
    {
        if( actualAssociation == null )
        {
            return null;
        }
        return actualAssociation.qualifiedName();
    }

    public boolean isImmutable()
    {
        return actualAssociation.isImmutable();
    }

    @Override
    public final String toString()
    {
        return name + "[" + type.getSimpleName() + "] -> " + stateModel.toString();
    }

    @SuppressWarnings( "unchecked" )
    public final void stateToUse( T aNewStateInUse )
    {
        // Update components
        for( Map.Entry<JComponent, FocusLostListener> entry : components.entrySet() )
        {
            JComponent component = entry.getKey();

            Class<? extends JComponent> componentClass = component.getClass();
            SwingAdapter<Association> adapter = adapters.get( componentClass );
            adapter.fromDataToSwing( component, actualAssociation );

            FocusLostListener focusListener = entry.getValue();
            focusListener.setAdapter( adapter );
        }

        if( actualAssociation == null ||
            actualAssociation.get() == aNewStateInUse )
        {
            return;
        }

        if( actualAssociation != null )
        {
            actualAssociation.set( aNewStateInUse );
        }

        stateModel.use( aNewStateInUse );
    }

    public final void fieldToUse( Association<T> anActualAssociation )
    {
        actualAssociation = anActualAssociation;

        T associationValue = null;
        if( anActualAssociation != null )
        {
            associationValue = anActualAssociation.get();
        }
        stateModel.use( associationValue );
    }

    public final <C extends JComponent> C to( C aComponent )
    {
        validateNotNull( "aComponent", aComponent );

        if( !components.containsKey( aComponent ) )
        {
            FocusLostListener focusListener = new FocusLostListener();

            // Sets the adapter for focus listener to use
            Class<? extends JComponent> componentClass = aComponent.getClass();
            SwingAdapter<Association> adapter = adapters.get( componentClass );

            if( adapter == null )
            {
                throw new IllegalBindingException( aComponent, type );
            }
            focusListener.setAdapter( adapter );

            // Initialized component initial value
            adapter.fromDataToSwing( aComponent, actualAssociation );

            aComponent.addFocusListener( focusListener );
            components.put( aComponent, focusListener );
        }

        return aComponent;
    }

    @Override
    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return capabilities.association;
    }

    private class FocusLostListener
        implements FocusListener
    {

        private SwingAdapter<Association> adapter;

        private void setAdapter( SwingAdapter adapterToUse )
        {
            adapter = adapterToUse;
        }

        public final void focusGained( FocusEvent e )
        {
        }

        public void focusLost( FocusEvent e )
        {
            if( adapter != null && actualAssociation != null )
            {
                JComponent component = (JComponent) e.getComponent();
                adapter.fromSwingToData( component, actualAssociation );
                stateToUse( actualAssociation.get() );
            }
        }
    }
}
