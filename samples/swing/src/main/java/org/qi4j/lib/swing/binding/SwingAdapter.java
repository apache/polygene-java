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

import java.util.Set;
import javax.swing.JComponent;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.property.Property;

public interface SwingAdapter
{
    Set<Capabilities> canHandle();

    void fromSwingToProperty( JComponent component, Property<?> property );

    void fromPropertyToSwing( JComponent component, Property<?> property );

    void fromSwingToAssociation( JComponent component, Association<?> property );

    void fromAssociationToSwing( JComponent component, Association<?> property );

    void fromSwingToSetAssociation( JComponent component, ManyAssociation<?> property );

    void fromSetAssociationToSwing( JComponent component, ManyAssociation<?> property );

    void fromSwingToNamedAssociation( JComponent component, NamedAssociation<?> namedAssociation );

    void fromNamedAssociationToSwing( JComponent component, NamedAssociation<?> namedAssociation );

    public class Capabilities
    {
        public final Class<? extends JComponent> component;
        public final Class<?> type;
        public final boolean property;
        public final boolean association;
        public final boolean listAssociation;
        public final boolean setAssociation;
        public final boolean namedAssociation;

        public Capabilities( Class<? extends JComponent> component, Class<?> type,
                             boolean property, boolean association, boolean setAssociation,
                             boolean listAssociation, boolean namedAssociation )
        {
            this.component = component;
            this.type = type;
            this.property = property;
            this.association = association;
            this.listAssociation = listAssociation;
            this.setAssociation = setAssociation;
            this.namedAssociation = namedAssociation;
        }
    }

}
