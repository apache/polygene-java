/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.api.query.operators;

import org.qi4j.api.query.Operator;
import org.qi4j.api.query.BinaryExpression;
import org.qi4j.api.query.ComparableExpression;

public class LessThanEquals
    implements Operator, BinaryExpression
{
    private ComparableExpression left;
    private ComparableExpression right;

    public LessThanEquals( ComparableExpression left, ComparableExpression right )
    {
        this.left = left;
        this.right = right;
    }

    public ComparableExpression getLeft()
    {
        return left;
    }

    public ComparableExpression getRight()
    {
        return right;
    }

}
