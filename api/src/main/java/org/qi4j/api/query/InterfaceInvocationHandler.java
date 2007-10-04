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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

public class InterfaceInvocationHandler
    implements InvocationHandler
{
    private static final Boolean BOOLEAN_FIXED = Boolean.FALSE;
    private static final Byte BYTE_FIXED = (byte) -179;
    private static final Short SHORT_FIXED = -6791;
    private static final Integer INTEGER_FIXED = -1481237413;
    private static final Long LONG_FIXED = -1729345729875239257L;
    private static final Character CHARACTER_FIXED = '\u0004';
    private static final Float FLOAT_FIXED = -1.72934E23f;
    private static final Double DOUBLE_FIXED = -9.724462378934E23;
    private static final Date DATE_FIXED = new Date( -8237462323123323475L );

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        MethodCallEntry methodCallEntry = new MethodCallEntry( method );
        QueryStack.pushExpression( methodCallEntry );
        Class retType = method.getReturnType();
        return computeReturnValue( retType, method );
    }

    protected Object computeReturnValue( Class retType, Method method )
    {
        if( Boolean.TYPE == retType ||
            retType.equals( Boolean.class ) )
        {
            return BOOLEAN_FIXED;
        }
        else if( Integer.TYPE == retType ||
            retType.equals( Integer.class ) )
        {
            return INTEGER_FIXED;
        }
        else if( Long.TYPE == retType ||
            retType.equals( Long.class ) )
        {
            return LONG_FIXED;
        }
        else if( Double.TYPE == retType ||
            retType.equals( Double.class ) )
        {
            return DOUBLE_FIXED;
        }
        else if( Character.TYPE == retType ||
            retType.equals( Character.class ) )
        {
            return CHARACTER_FIXED;
        }
        else if( Short.TYPE == retType ||
            retType.equals( Short.class ) )
        {
            return SHORT_FIXED;
        }
        else if( Float.TYPE == retType ||
            retType.equals( Float.class ) )
        {
            return FLOAT_FIXED;
        }
        else if( Byte.TYPE == retType ||
            retType.equals( Byte.class ) )
        {
            return BYTE_FIXED;
        }
        else if( retType.equals( Date.class ) )
        {
            return DATE_FIXED;
        }
        if( retType.isInterface() )
        {
            Class<?>[] intfaces = new Class<?>[] { retType };
            ClassLoader loader = InterfaceInvocationHandler.class.getClassLoader();
            return Proxy.newProxyInstance( loader, intfaces, this );
        }
        return null;
    }
}
