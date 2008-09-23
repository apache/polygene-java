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
package org.qi4j.library.swing.visualizer.detailPanel;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.detailPanel.internal.DefaultDisplayManager;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
public final class DetailPanel extends JPanel
{
    private DisplayManager manager;

    public DetailPanel()
    {
        super( new BorderLayout() );
        manager = new DefaultDisplayManager();
    }

    public final void setDisplayManager( DisplayManager aManager )
        throws IllegalArgumentException
    {
        validateNotNull( "aManager", aManager );
        manager = aManager;
    }

    public final void display( CompositeDetailDescriptor aDescriptor )
    {
        manager.display( this, aDescriptor );
    }
}
