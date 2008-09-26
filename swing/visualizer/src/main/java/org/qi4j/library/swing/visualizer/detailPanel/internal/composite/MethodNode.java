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
package org.qi4j.library.swing.visualizer.detailPanel.internal.composite;

import java.lang.reflect.Method;
import javax.swing.tree.DefaultMutableTreeNode;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import static org.qi4j.library.swing.visualizer.detailPanel.internal.common.ToStringUtils.methodToString;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;

/**
 * @author Sonny Gill
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class MethodNode extends DefaultMutableTreeNode
{
    private final Method method;
    private final CompositeMethodDetailDescriptor detailDescriptor;

    MethodNode( Method aMethod, CompositeMethodDetailDescriptor aDescriptor )
        throws IllegalArgumentException
    {
        method = aMethod;
        validateNotNull( "aMethod", aMethod );

        detailDescriptor = aDescriptor;
    }

    /**
     * @return Method detail descriptor. Returns {@code null} if not found.
     * @since 0.5
     */
    final CompositeMethodDetailDescriptor descriptor()
    {
        return detailDescriptor;
    }

    public final String toString()
    {
        String methodSignature = methodToString( method );
        if( detailDescriptor == null )
        {
            methodSignature += " [IH]";
        }
        return methodSignature;
    }
}
