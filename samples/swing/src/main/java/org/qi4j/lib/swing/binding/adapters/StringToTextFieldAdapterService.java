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
package org.qi4j.lib.swing.binding.adapters;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.mixin.NoopMixin;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.lib.swing.binding.SwingAdapter;

@Concerns( StringToTextFieldAdapterService.StringToTextFieldAdapterConcern.class )
@Mixins( NoopMixin.class )
public interface StringToTextFieldAdapterService
    extends SwingAdapter, ServiceComposite
{

    class StringToTextFieldAdapterConcern
        extends ConcernOf<SwingAdapter>
        implements SwingAdapter
    {

        private HashSet<Capabilities> canHandle;

        public StringToTextFieldAdapterConcern()
        {
            canHandle = new HashSet<>();
            canHandle.add( new Capabilities( JTextArea.class, String.class, true, false, false, false, false ) );
            canHandle.add( new Capabilities( JTextField.class, String.class, true, false, false, false, false ) );
            canHandle.add( new Capabilities( JLabel.class, String.class, true, false, false, false, false ) );
        }

        @Override
        public Set<Capabilities> canHandle()
        {
            return canHandle;
        }

        @Override
        public void fromSwingToProperty( JComponent component, Property property )
        {
            if( property == null )
            {
                return;
            }
            if( component instanceof JTextComponent )
            {
                JTextComponent textComponent = (JTextComponent) component;
                property.set( textComponent.getText() );
            }
            else
            {
                JLabel labelComponent = (JLabel) component;
                property.set( labelComponent.getText() );
            }
        }

        @Override
        public void fromPropertyToSwing( JComponent component, Property<?> property )
        {
            String value;
            if( property == null )
            {
                value = "";
            }
            else
            {
                value = (String) property.get();
            }
            if( component instanceof JTextComponent )
            {
                JTextComponent textComponent = (JTextComponent) component;
                textComponent.setText( value );
            }
            else
            {
                JLabel labelComponent = (JLabel) component;
                labelComponent.setText( value );
            }
        }

        @Override
        public void fromSwingToAssociation( JComponent component, Association<?> association )
        {
        }

        @Override
        public void fromAssociationToSwing( JComponent component, Association<?> association )
        {
        }

        @Override
        public void fromSwingToSetAssociation( JComponent component, ManyAssociation<?> setAssociation )
        {
        }

        @Override
        public void fromSetAssociationToSwing( JComponent component, ManyAssociation<?> setAssociation )
        {
        }

        @Override
        public void fromSwingToNamedAssociation( JComponent component, NamedAssociation<?> namedAssociation )
        {
        }

        @Override
        public void fromNamedAssociationToSwing( JComponent component, NamedAssociation<?> namedAssociation )
        {
        }

    }

}
