/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.lib.swing.binding.internal;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.lib.swing.binding.SwingAdapter;
import org.qi4j.property.Property;

/**
 * @author edward.yakop@gmail.com
 */
public abstract class SwingAdapterMixin
    implements SwingAdapter
{

    private HashSet<Capabilities> capabilitiesSet;

    public Set<Capabilities> canHandle()
    {
        if( capabilitiesSet == null )
        {
            capabilitiesSet = new HashSet<Capabilities>();
        }

        return capabilitiesSet;
    }

    public void fromSwingToProperty( JComponent component, Property property )
    {
        // Do nothing
    }

    public void fromPropertyToSwing( JComponent component, Property property )
    {
        // Do nothing
    }

    public void fromSwingToAssociation( JComponent component, Association anAssociation )
    {
        // Do nothing
    }

    public void fromAssociationToSwing( JComponent aComponent, Association anAssociation )
    {
        // Do nothing
    }

    public void fromSwingToSetAssociation( JComponent component, SetAssociation setAssociation )
    {
        // Do nothing
    }

    public void fromSetAssociationToSwing( JComponent component, SetAssociation setAssociation )
    {
        // Do nothing
    }

    public void fromSwingToListAssociation( JComponent component, ListAssociation listAssociation )
    {
        // Do nothing
    }

    public void fromListAssociationToSwing( JComponent component, ListAssociation listAssociation )
    {
        // Do nothing
    }
}
