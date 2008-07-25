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
package org.qi4j.lib.swing.binding;

import static java.lang.reflect.Proxy.newProxyInstance;
import java.util.Map;
import javax.swing.JComponent;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.Uses;
import org.qi4j.lib.swing.binding.internal.BoundField;
import org.qi4j.lib.swing.binding.internal.StateInvocationHandler;
import org.qi4j.lib.swing.binding.internal.association.BoundAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundListAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundManyAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundManyAssociationTableDelegate;
import org.qi4j.lib.swing.binding.internal.association.BoundSetAssociation;
import org.qi4j.lib.swing.binding.internal.association.DefaultBoundListAssociationDelegate;
import org.qi4j.lib.swing.binding.internal.association.DefaultBoundManyAssociationDelegate;
import org.qi4j.lib.swing.binding.internal.association.DefaultBoundSetAssociationDelegate;
import org.qi4j.lib.swing.binding.internal.property.BoundProperty;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.property.Property;

public final class StateModel<T>
{

    private @Uses( optional = true ) BoundField fieldOwnedThis;

    private final Class<T> type;
    private final ObjectBuilderFactory obf;
    private final StateInvocationHandler childModel;

    private T currentData;

    public StateModel( @Uses Class<T> aType, @Structure ObjectBuilderFactory anObjectBuilderFactory )
    {
        type = aType;
        obf = anObjectBuilderFactory;

        ObjectBuilder<StateInvocationHandler> objectBuilder = obf.newObjectBuilder( StateInvocationHandler.class );
        objectBuilder.use( aType );
        childModel = objectBuilder.newInstance();
    }

    /**
     * Sets the data to this component model and its children.
     *
     * @param aData The data to set.
     */
    @SuppressWarnings( "unchecked" )
    public void use( T aData )
    {
        if( fieldOwnedThis != null )
        {
            fieldOwnedThis.stateToUse( aData );
        }

        currentData = aData;
        childModel.use( aData );
    }

    public T stateInUse()
    {
        return currentData;
    }

    @SuppressWarnings( "unchecked" )
    public final T state()
    {
        ClassLoader loader = StateInvocationHandler.class.getClassLoader();
        Class[] typeInterfaces = new Class[]{ type };
        return (T) newProxyInstance( loader, typeInterfaces, childModel );
    }

    @SuppressWarnings( "unchecked" )
    public <T> SwingBinding<T> bind( Property<T> aProperty )
    {
        validateNotNull( "aProperty", aProperty );
        if( aProperty instanceof BoundProperty )
        {
            return (BoundProperty) aProperty;
        }
        throw new IllegalArgumentException( "Unknown property template: " + aProperty );
    }

    @SuppressWarnings( "unchecked" )
    public <T> SwingBinding<T> bind( Association<T> anAssociation )
    {
        validateNotNull( "anAssociation", anAssociation );

        if( anAssociation instanceof BoundAssociation )
        {
            return (BoundAssociation) anAssociation;
        }

        throw new IllegalArgumentException( "Unknown association template: " + anAssociation );
    }

    public <T> ToggleButtonBinding<T> bindToggleButton( Property<T> aProperty )
    {
        //TODO
        return null;
    }

    public <T> SwingBinding<T> bind( SetAssociation<T> anAssociation )
    {
        validateNotNull( "anAssociation", anAssociation );

        if( anAssociation instanceof BoundSetAssociation )
        {
            return bind( anAssociation, DefaultBoundSetAssociationDelegate.class );
        }

        throw new IllegalArgumentException( "Unknown many association template: " + anAssociation );
    }

    public <T> SwingBinding<T> bind( ListAssociation<T> anAssociation )
    {
        validateNotNull( "anAssociation", anAssociation );

        if( anAssociation instanceof BoundListAssociation )
        {
            return bind( anAssociation, DefaultBoundListAssociationDelegate.class );
        }

        throw new IllegalArgumentException( "Unknown many association template: " + anAssociation );
    }

    @SuppressWarnings( "unchecked" )
    private <T> SwingBinding<T> bind( ManyAssociation<T> anAssociation,
                                      Class<? extends DefaultBoundManyAssociationDelegate> classType )
    {
        BoundManyAssociation association = (BoundManyAssociation) anAssociation;

        // Create new delegate
        ObjectBuilder<DefaultBoundManyAssociationDelegate> delegateBuilder =
            (ObjectBuilder<DefaultBoundManyAssociationDelegate>) obf.newObjectBuilder( classType );

        Map<Class<? extends JComponent>, SwingAdapter> adapters = association.adapters();
        delegateBuilder.use( adapters );

        DefaultBoundManyAssociationDelegate delegate = delegateBuilder.newInstance();

        association.delegate( delegate );

        return delegate;
    }

    //TODO boonping. should not use ManyAssociation.
    @SuppressWarnings( "unchecked" )
    public TableBinding bindTable( ManyAssociation anAssociation )
    {
        validateNotNull( "anAssociation", anAssociation );

        if( anAssociation instanceof BoundManyAssociation )
        {
            BoundManyAssociation bound = (BoundManyAssociation) anAssociation;

            BoundManyAssociationTableDelegate delegate =
                new BoundManyAssociationTableDelegate( bound.stateModel() );

            bound.delegate( delegate );

            return delegate;
        }

        throw new IllegalArgumentException( "Unknown ManyAssociation template: " + anAssociation );
    }
}
