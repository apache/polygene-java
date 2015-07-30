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
package org.apache.zest.sample.swing.binding;

import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.sample.swing.binding.adapters.StringToTextFieldAdapterService;
import org.apache.zest.sample.swing.binding.internal.BoundProperty;
import org.apache.zest.sample.swing.binding.internal.StateInvocationHandler;

public class SwingBindingAssembler
    implements Assembler
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( StateModel.class, StateInvocationHandler.class, BoundProperty.class );
        addStringToTextFieldAdapter( module );
    }

    protected void addStringToTextFieldAdapter( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( StringToTextFieldAdapterService.class );
    }
}
