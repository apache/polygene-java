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
import java.awt.Component;
import javax.swing.JPanel;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.detailPanel.internal.DefaultDisplayManager;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.EntityDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.LayerDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ModuleDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ObjectDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.ServiceDetailDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @author Sonny Gill
 * @since 0.5
 */
public final class DetailPanel extends JPanel
{
    private DisplayManager manager;
    private Component component;

    public DetailPanel()
    {
        super( new BorderLayout() );

        manager = new DefaultDisplayManager();
        component = null;
    }

    public final void setDisplayManager( DisplayManager aManager )
        throws IllegalArgumentException
    {
        validateNotNull( "aManager", aManager );
        manager = aManager;
    }

    public void displayApplication( ApplicationDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        manager.displayApplication( this, aDescriptor );
    }

    public final void displayLayer( LayerDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        manager.displayLayer( this, aDescriptor );
    }

    public final void displayModule( ModuleDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );
        manager.displayModule( this, aDescriptor );
    }

    public final void displayService( ServiceDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        manager.displayService( this, aDescriptor );
    }

    public final void displayEntity( EntityDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        manager.displayEntity( this, aDescriptor );
    }

    public final void displayComposite( CompositeDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        manager.displayComposite( this, aDescriptor );
    }

    public final void displayObject( ObjectDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        manager.displayObject( this, aDescriptor );
    }

    public final void updateContent( Component aComponent )
    {
        boolean isChange = false;

        if( aComponent != null )
        {
            add( aComponent, BorderLayout.CENTER );
            isChange = true;
        }

        if( component != null )
        {
            remove( component );
            isChange = true;
        }

        component = aComponent;

        if( isChange )
        {
            revalidate();
            repaint();
        }
    }
}
