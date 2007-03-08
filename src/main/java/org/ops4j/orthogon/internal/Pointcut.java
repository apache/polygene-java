/*  Copyright 2007 Niclas Hedhman.
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
package org.ops4j.orthogon.internal;

import java.util.List;
import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;

final class Pointcut
{
    private Pointcut m_parent;
    private List<QiImplements> m_implementsConstraints;
    private List<QiTargetClass> m_targetClassConstraints;
    private List<QiProperty> m_propertyConstraints;
    private List<QiMethodExpression> m_methodExpressionConstraints;

    public Pointcut( Pointcut parent, List<QiImplements> anImplements, List<QiProperty> property,
                     List<QiMethodExpression> methodExpressions,
                     List<QiTargetClass> targetClasses
    )
    {
        m_parent = parent;
        m_implementsConstraints = anImplements;
        m_propertyConstraints = property;
        m_methodExpressionConstraints = methodExpressions;
        m_targetClassConstraints = targetClasses;
    }

    public Pointcut getParent()
    {
        return m_parent;
    }

    public List<QiImplements> getImplementsConstraints()
    {
        return m_implementsConstraints;
    }

    public List<QiProperty> getPropertyConstraints()
    {
        return m_propertyConstraints;
    }

    public List<QiMethodExpression> getMethodExpressionConstraints()
    {
        return m_methodExpressionConstraints;
    }

    public List<QiTargetClass> getTargetClassConstraints()
    {
        return m_targetClassConstraints;
    }
}
