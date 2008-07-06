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
import org.qi4j.composite.Mixins;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.lib.swing.binding.internal.SwingAdapterMixin;
import org.qi4j.property.Property;

@Mixins( SwingAdapterMixin.class )
public interface SwingAdapter<T>
{

    Set<Capabilities> canHandle();

    void fromSwingToData( JComponent aComponent, T data );

    void fromDataToSwing( JComponent aComponent, T data );
//
//    void fromSwingToAssociation( JComponent aComponent, Association anAssociation );
//
//    void fromAssociationToSwing( JComponent aComponent, Association anAssociation );
//
//    void fromSwingToSetAssociation( JComponent component, SetAssociation setAssociation );
//
//    void fromSetAssociationToSwing( JComponent component, SetAssociation setAssociation );
//
//    void fromSwingToListAssociation( JComponent component, ListAssociation listAssociation );
//
//    void fromListAssociationToSwing( JComponent component, ListAssociation listAssociation );

    public class Capabilities
    {

        public Class<? extends JComponent> component;
        public Class<?> fieldType;

        public boolean property;
        public boolean association;
        public boolean listAssociation;
        public boolean setAssociation;

        public Capabilities( Class<? extends JComponent> aComponent, Class<?> aFieldType,
                             boolean isPropertySupported, boolean isAssociationSupported,
                             boolean isSetAssociationSupported, boolean isListAssociationSupported )
        {
            component = aComponent;
            fieldType = aFieldType;

            property = isPropertySupported;
            association = isAssociationSupported;
            listAssociation = isListAssociationSupported;
            setAssociation = isSetAssociationSupported;
        }
    }


}
