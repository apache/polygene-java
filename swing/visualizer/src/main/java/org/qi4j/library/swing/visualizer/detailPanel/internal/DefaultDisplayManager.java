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
package org.qi4j.library.swing.visualizer.detailPanel.internal;

import org.qi4j.library.swing.visualizer.detailPanel.DetailPanel;
import org.qi4j.library.swing.visualizer.detailPanel.DisplayManager;
import org.qi4j.library.swing.visualizer.detailPanel.internal.application.ApplicationDetailPanel;
import org.qi4j.library.swing.visualizer.detailPanel.internal.composite.CompositeDetailPanel;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.service.ServiceDescriptor;

/**
 * @author edward.yakop@gmail.com
 */
public final class DefaultDisplayManager
    implements DisplayManager
{
    public final void displayApplication( DetailPanel aDetailPanel, ApplicationDetailDescriptor aDescriptor )
    {
        ApplicationDetailPanel panel = new ApplicationDetailPanel( aDescriptor );
        aDetailPanel.updateContent( panel );
    }

    public final void displayLayer( DetailPanel aDetailPanel, LayerDetailDescriptor aDescriptor )
    {
        System.err.println( "Layer" );
    }

    public final void displayModule( DetailPanel aDetailPanel, ModuleDetailDescriptor aDescriptor )
    {
        System.err.println( "Module" );
    }

    public final void displayService( DetailPanel aDetailPanel, ServiceDescriptor aDescriptor )
    {
        System.err.println( "Service" );
    }

    public final void displayEntity( DetailPanel aDetailPanel, EntityDetailDescriptor aDescriptor )
    {
        CompositeDetailPanel panel = new CompositeDetailPanel( aDescriptor );
        aDetailPanel.updateContent( panel );
    }

    public final void displayComposite( DetailPanel aDetailPanel, CompositeDetailDescriptor aDescriptor )
    {
        CompositeDetailPanel panel = new CompositeDetailPanel( aDescriptor );
        aDetailPanel.updateContent( panel );
    }

    public final void displayObject( DetailPanel aDetailPanel, ObjectDetailDescriptor aDescriptor )
    {
        System.err.println( "Object" );
    }
}
