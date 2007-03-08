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

import org.ops4j.orthogon.pointcut.constraints.QiImplements;
import org.ops4j.orthogon.pointcut.constraints.QiProperty;
import org.ops4j.orthogon.pointcut.constraints.QiMethodExpression;
import org.ops4j.orthogon.pointcut.constraints.QiTargetClass;
import java.util.List;
import java.util.ArrayList;

public class PointcutBuilder
{
    private List<QiImplements> m_implements;
    private List<QiProperty> m_property;
    private List<QiMethodExpression> m_methodExpressions;
    private List<QiTargetClass> m_targetClasses;
    private Pointcut m_parent;

    public PointcutBuilder( Pointcut parent )
    {
        m_parent = parent;
    }

    void addImplementsConstraint( QiImplements implementsConstraint )
    {
        m_implements.add( implementsConstraint );
    }

    void addPropertyConstraint( QiProperty propertyConstraint )
    {
        m_property.add( propertyConstraint );
    }

    void addMethodExpressionConstraint( QiMethodExpression methodExpressionConstraint )
    {
        m_methodExpressions.add( methodExpressionConstraint );
    }

    void addTargetClassConstraint( QiTargetClass targetClassConstraint )
    {
        m_targetClasses.add( targetClassConstraint );
    }

    Pointcut create()
    {
        return new Pointcut( m_parent, m_implements, m_property, m_methodExpressions, m_targetClasses );
    }
}
