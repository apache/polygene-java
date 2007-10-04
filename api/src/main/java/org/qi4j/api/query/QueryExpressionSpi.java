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
package org.qi4j.api.query;

public interface QueryExpressionSpi
{
    BinaryExpression eq( Object left, Object right );

    BinaryExpression ne( Object left, Object right );

    BinaryExpression lt( Number left, Number right );

    BinaryExpression gt( Number left, Number right );

    BinaryExpression le( Number left, Number right );

    BinaryExpression ge( Number left, Number right );

    BinaryExpression matches( String source, String expression );

    <K> BinaryExpression contains( Iterable<K> left, K right );

    BinaryExpression contains( String value, String substring );

    BinaryExpression isNull( Object value );

    BinaryExpression isNotNull( Object value );

    BinaryExpression and( BinaryExpression left, BinaryExpression right );

    BinaryExpression or( BinaryExpression left, BinaryExpression right);

    BinaryExpression not( BinaryExpression expr );
}
