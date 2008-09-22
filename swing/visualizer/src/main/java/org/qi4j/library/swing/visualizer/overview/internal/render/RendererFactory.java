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
package org.qi4j.library.swing.visualizer.overview.internal.render;

import org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.FIELD_TYPE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.APPLICATION;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.COMPOSITE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.EDGE_HIDDEN;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.ENTITY;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.GROUP;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.LAYER;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.MODULE;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.OBJECT;
import static org.qi4j.library.swing.visualizer.overview.internal.common.GraphConstants.NodeType.SERVICE;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import static prefuse.data.expression.ComparisonPredicate.EQ;
import prefuse.data.expression.ObjectLiteral;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.NullRenderer;

/**
 * @author edward.yakop@gmail.com
 */
public final class RendererFactory extends DefaultRendererFactory
{
    public RendererFactory()
    {
        add( createPredicate( APPLICATION ), new ApplicationRenderer() );
        add( createPredicate( LAYER ), new LayerRenderer() );
        add( createPredicate( MODULE ), new ModuleRenderer() );

        add( createPredicate( COMPOSITE ), new ObjectRenderer() );
        add( createPredicate( ENTITY ), new ObjectRenderer() );
        add( createPredicate( OBJECT ), new ObjectRenderer() );
        add( createPredicate( SERVICE ), new ObjectRenderer() );

        add( createPredicate( GROUP ), new GroupRenderer() );
        add( createPredicate( EDGE_HIDDEN ), new NullRenderer() );

        setDefaultEdgeRenderer( new VerticalEdgeRenderer() );
    }

    private static ComparisonPredicate createPredicate( GraphConstants.NodeType value )
    {
        return new ComparisonPredicate( EQ, new ColumnExpression( FIELD_TYPE ), new ObjectLiteral( value ) );
    }
}