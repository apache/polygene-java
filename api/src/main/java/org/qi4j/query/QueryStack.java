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
package org.qi4j.query;

import java.util.Stack;

public class QueryStack<T> extends Stack<T>
{
    private static ThreadLocal<Stack<Expression>> stack;
    private static int counter;
    private final String name;

    public QueryStack( int sequence )
    {
        name = "Stack" + sequence;
    }

    static
    {
        stack = new ThreadLocal<Stack<Expression>>();
    }

    static void pushExpression( Expression expression )
    {
        Stack<Expression> st = getStack();
        System.out.println( st + "  <--push---  " + expression );
        st.push( expression );
    }

    static Expression popExpression()
    {
        Stack<Expression> st = getStack();
        Expression value = st.pop();
        System.out.println( st + "  ---pop-->  " + value );
        return value;
    }

    private static Stack<Expression> getStack()
    {
        Stack<Expression> st = stack.get();
        if( st == null )
        {
            st = new QueryStack<Expression>( counter++ );
            stack.set( st );
        }
        return st;
    }

    public String toString()
    {
        return name + "[" + super.toString() + "]";
    }

    public static int getSize()
    {
        return getStack().size();
    }
}
