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

import org.qi4j.lib.swing.binding.SwingAdapter;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.qi4j.composite.Concerns;
import org.qi4j.composite.Mixins;
import org.qi4j.composite.NoopMixin;
import org.qi4j.composite.ConcernOf;
import org.qi4j.property.Property;
import org.qi4j.service.ServiceComposite;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.entity.association.ListAssociation;
import java.util.Set;
import java.util.HashSet;

@Concerns( StringToTextFieldAdapterService.StringToTextFieldAdapterConcern.class )
@Mixins( NoopMixin.class )
public interface StringToTextFieldAdapterService extends SwingAdapter, ServiceComposite
{

    class StringToTextFieldAdapterConcern extends ConcernOf<SwingAdapter>
        implements SwingAdapter
    {

        private HashSet<Capabilities> canHandle;

        public StringToTextFieldAdapterConcern()
        {
            canHandle = new HashSet<Capabilities>();
            canHandle.add( new Capabilities( JTextArea.class, String.class, true, false, false, false ) );
            canHandle.add( new Capabilities( JTextField.class, String.class, true, false, false, false ) );
            canHandle.add( new Capabilities( JLabel.class, String.class, true, false, false, false ) );
        }

        public Set<Capabilities> canHandle()
        {
            return canHandle;
        }

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

        public void fromSwingToAssociation( JComponent component, Association<?> association )
        {
        }

        public void fromAssociationToSwing( JComponent component, Association<?> association )
        {
        }

        public void fromSwingToSetAssociation( JComponent component, SetAssociation<?> setAssociation )
        {
        }

        public void fromSetAssociationToSwing( JComponent component, SetAssociation<?> setAssociation )
        {
        }

        public void fromSwingToListAssociation( JComponent component, ListAssociation<?> listAssociation )
        {
        }

        public void fromListAssociationToSwing( JComponent component, ListAssociation<?> listAssociation )
        {
        }
    }
}
