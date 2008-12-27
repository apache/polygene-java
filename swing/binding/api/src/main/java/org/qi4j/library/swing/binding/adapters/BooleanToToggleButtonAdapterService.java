/*
 * Copyright 2008 Lan Boon Ping. All rights Reserved.
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
package org.qi4j.library.swing.binding.adapters;

import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.library.swing.binding.SwingAdapter;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.ServiceComposite;

@Concerns( BooleanToToggleButtonAdapterService.BooleanToToggleButtonAdapterServiceMixin.class )
public interface BooleanToToggleButtonAdapterService extends SwingAdapter, ServiceComposite
{
    class BooleanToToggleButtonAdapterServiceMixin extends ConcernOf<SwingAdapter>
        implements SwingAdapter<Property>
    {
        public Set<Capabilities> canHandle()
        {
            Set<Capabilities> canHandle = next.canHandle();
            canHandle.add( new Capabilities( JCheckBox.class, Boolean.class, true, false, false, false ) );
            canHandle.add( new Capabilities( JRadioButton.class, Boolean.class, true, false, false, false ) );
            return canHandle;
        }

        @SuppressWarnings( "unchecked" )
        public void fromSwingToData( JComponent aComponent, Property aProperty )
        {
            if( aProperty != null )
            {
                JToggleButton button = (JToggleButton) aComponent;
                aProperty.set( button.isSelected() );
            }
        }

        @SuppressWarnings( "unchecked" )
        public void fromDataToSwing( JComponent aComponent, Property aProperty )
        {
            if( aProperty != null )
            {
                JToggleButton button = (JToggleButton) aComponent;
                Boolean newValue = (Boolean) aProperty.get();
                button.setSelected( newValue );
            }
        }
    }
}
