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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.assembler;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.common.assembler.FormCommonAssembler;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.composite.assembler.CompositeFormAssembler;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.entity.assembler.EntityFormAssembler;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.layer.assembler.LayerFormAssembler;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.module.assembler.ModuleFormAssembler;
import org.qi4j.library.swing.visualizer.detailPanel.internal.form.object.assembler.ObjectFormAssembler;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class FormAssembler
    implements Assembler
{
    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addAssembler( new FormCommonAssembler() );
        aModule.addAssembler( new CompositeFormAssembler() );
        aModule.addAssembler( new EntityFormAssembler() );
        aModule.addAssembler( new LayerFormAssembler() );
        aModule.addAssembler( new ModuleFormAssembler() );
        aModule.addAssembler( new ObjectFormAssembler() );
    }
}
