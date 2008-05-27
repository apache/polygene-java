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
package org.qi4j.quikit.panels.entityList;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.quikit.panels.entityList.actions.add.AddCompositeLink;
import static org.qi4j.structure.Visibility.application;
import static org.qi4j.structure.Visibility.module;

/**
 * @author edward.yakop@gmail.com
 */
public final class EntityListViewPanelAssembler
    implements Assembler
{
    public final void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        // Entity list view panel
        ObjectDeclaration entityListViewPanelReg = aModule.addObjects(
            EntityListViewPanel.class );
        entityListViewPanelReg.visibleIn( application );

        // Data provider
        ObjectDeclaration entityListViewPanelDataProviderReg = aModule.addObjects(
            EntityDataProvider.class,
            EntityPropertyLabelProvider.class,
            EntityPropertyValueDataProvider.class );
        entityListViewPanelDataProviderReg.visibleIn( module );

        // Actions
        ObjectDeclaration compositeActionReg = aModule.addObjects(
            AddCompositeLink.class
        );
        compositeActionReg.visibleIn( module );

    }
}
