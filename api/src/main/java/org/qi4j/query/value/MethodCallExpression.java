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
package org.qi4j.query.value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.qi4j.query.QueryEvaluationException;

public class MethodCallExpression
    implements ValueExpression
{
    private Method method;
    private Object[] arguments;

    public MethodCallExpression( Method method, Object[] args )
    {
        this.method = method;
        this.arguments = args;
    }

    public Method getMethod()
    {
        return method;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public Object getValue( Object candidate, Map<String, Object> variables )
    {
        try
        {
            return method.invoke( candidate, arguments );
        }
        catch( IllegalAccessException e )
        {
            throw new QueryEvaluationException( e );
        }
        catch( InvocationTargetException e )
        {
            throw new QueryEvaluationException( e.getTargetException() );
        }
    }

    public String toString()
    {
        Class c = method.getDeclaringClass();
        String name = c.getName();
        int pos = name.lastIndexOf( "." );
        return name.substring( pos + 1 ) + "." + method.getName() + "()";
    }
}
