/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman
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
package org.qi4j.index.sql.internal;

import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.GreaterThanPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;
import org.qi4j.api.query.grammar.LessThanPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.NotEqualsPredicate;
import org.qi4j.api.query.grammar.Predicate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mathcing an operator based on a predicate.
 */
class Operators
{
    private static final Map<Class<? extends Predicate>, String> m_operators;

    static
    {
        m_operators = new HashMap<Class<? extends Predicate>, String>();
        m_operators.put( EqualsPredicate.class, "=" );
        m_operators.put( GreaterOrEqualPredicate.class, ">=" );
        m_operators.put( GreaterThanPredicate.class, ">" );
        m_operators.put( LessOrEqualPredicate.class, "<=" );
        m_operators.put( LessThanPredicate.class, "<" );
        m_operators.put( NotEqualsPredicate.class, "!=" );
        m_operators.put( ManyAssociationContainsPredicate.class, "=" );
    }

    /**
     * Private constructor. Utility class.
     */
    private Operators()
    {

    }

    static String getOperator( final Class<? extends Predicate> predicateClass )
    {
        String operator = null;
        for( Map.Entry<Class<? extends Predicate>, String> entry : m_operators.entrySet() )
        {
            if( entry.getKey().isAssignableFrom( predicateClass ) )
            {
                operator = entry.getValue();
                break;
            }
        }
        if( operator == null )
        {
            throw new UnsupportedOperationException( "Predicate [" + predicateClass.getName() + "] is not supported" );
        }
        return operator;
    }

}