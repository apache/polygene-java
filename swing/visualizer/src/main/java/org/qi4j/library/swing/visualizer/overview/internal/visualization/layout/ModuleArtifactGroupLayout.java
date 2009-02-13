/*
 * Copyright 2008 Edward Yakop. All Rights Reserved.
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
package org.qi4j.library.swing.visualizer.overview.internal.visualization.layout;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import prefuse.visual.NodeItem;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ModuleArtifactGroupLayout
    extends AbstractContainerLayout<ModuleArtifactLayout>
{
    private final Set<ModuleArtifactLayout> moduleArtifacts;

    ModuleArtifactGroupLayout( NodeItem aModuleArtifactGroup )
        throws IllegalArgumentException
    {
        super( aModuleArtifactGroup );

        Iterator childrenIt = nodeItem.children();
        moduleArtifacts = new TreeSet<ModuleArtifactLayout>( LABEL_COMPARATOR );
        while( childrenIt.hasNext() )
        {
            NodeItem child = (NodeItem) childrenIt.next();
            moduleArtifacts.add( new ModuleArtifactLayout( child ) );
        }
    }

    @Override
    protected final Iterable<ModuleArtifactLayout> children()
    {
        return moduleArtifacts;
    }

    @Override
    protected int childrenCount()
    {
        return moduleArtifacts.size();
    }

    @Override
    public final Rectangle2D applyLayout( LayoutConstraint constraint )
    {
        //return arrangeChildrenVertically( constraint );
        return arrangeChildrenVertically( constraint, nodeItem.getVisualization().getDisplay( 0 ).getScale() );
    }

    @Override
    public final Dimension preferredDimension()
    {

        //return preferredDimensionIfChildrenArrangedVertically();
        return preferredDimensionIfChildrenArrangedVertically(nodeItem.getVisualization().getDisplay( 0 ).getScale());
    }
}
