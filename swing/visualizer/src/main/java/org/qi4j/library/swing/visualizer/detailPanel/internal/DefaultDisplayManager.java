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

import org.qi4j.library.swing.visualizer.detailPanel.DisplayManager;
import org.qi4j.library.swing.visualizer.detailPanel.DetailPanel;
import org.qi4j.library.swing.visualizer.detailPanel.internal.composite.CompositeOverviewPanel;
import org.qi4j.library.swing.visualizer.overview.descriptor.CompositeDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 */
public class DefaultDisplayManager
    implements DisplayManager
{
    public void display( DetailPanel aPanel, CompositeDetailDescriptor aDescriptor )
    {
        aPanel.setLeftComponent( new CompositeOverviewPanel( aPanel, aDescriptor ) );
        aPanel.setRightComponent( aPanel.createHelpPanel() );
    }
}
