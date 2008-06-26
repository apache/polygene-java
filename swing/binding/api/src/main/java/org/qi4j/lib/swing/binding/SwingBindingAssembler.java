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

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.lib.swing.binding.adapters.StringToTextFieldAdapterService;
import org.qi4j.lib.swing.binding.internal.StateInvocationHandler;
import org.qi4j.lib.swing.binding.internal.association.BoundAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundListAssociation;
import org.qi4j.lib.swing.binding.internal.association.BoundSetAssociation;
import org.qi4j.lib.swing.binding.internal.association.DefaultListAssociationDelegate;
import org.qi4j.lib.swing.binding.internal.association.DefaultSetAssociationDelegate;
import org.qi4j.lib.swing.binding.internal.property.BoundProperty;
import static org.qi4j.structure.Visibility.application;

public class SwingBindingAssembler
    implements Assembler
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects(
            StateModel.class
        ).visibleIn( application );

        module.addObjects(
            BoundAssociation.class,
            BoundProperty.class,
            BoundSetAssociation.class,
            BoundListAssociation.class,
            DefaultListAssociationDelegate.class,
            DefaultSetAssociationDelegate.class,
            StateInvocationHandler.class
        );

        addStringToTextFieldAdapter( module );
    }

    protected void addStringToTextFieldAdapter( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( StringToTextFieldAdapterService.class );
    }
}
