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
package org.qi4j.library.swing.visualizer.detailPanel.internal.application;

import javax.swing.JScrollPane;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.model.ApplicationDetailDescriptor;
import org.qi4j.library.swing.visualizer.detailPanel.internal.common.Qi4jDescriptorTree;

/**
 * TODO Javadoc
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ApplicationDetailPanel extends JScrollPane
{
    public ApplicationDetailPanel( ApplicationDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        validateNotNull( "aDescriptor", aDescriptor );

        Qi4jDescriptorTree appTree = new Qi4jDescriptorTree( new ApplicationTreeNode( aDescriptor ) );
        setViewportView( appTree );
    }
}
