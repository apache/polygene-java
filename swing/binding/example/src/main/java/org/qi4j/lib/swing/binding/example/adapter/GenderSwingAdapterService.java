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
package org.qi4j.lib.swing.binding.example.adapter;

import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import org.qi4j.composite.Mixins;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.lib.swing.binding.adapters.AbstractSwingAdapter;
import org.qi4j.lib.swing.binding.example.Gender;
import org.qi4j.property.Property;
import org.qi4j.service.ServiceComposite;

@Mixins( GenderSwingAdapterService.GenderSwingAdapterServiceMixin.class )
public interface GenderSwingAdapterService extends SwingAdapter, ServiceComposite
{
    abstract class GenderSwingAdapterServiceMixin extends AbstractSwingAdapter
    {
        protected void canHandle( Set<Capabilities> canHandle )
        {
            canHandle.add( new Capabilities( JRadioButton.class, Gender.class, true, false, false, false ) );
        }

        public void fromSwingToProperty( JComponent aComponent, Property aProperty )
        {
            //TODO
        }

        public void fromPropertyToSwing( JComponent aComponent, Property aProperty )
        {
            //TODO
        }
    }
}
