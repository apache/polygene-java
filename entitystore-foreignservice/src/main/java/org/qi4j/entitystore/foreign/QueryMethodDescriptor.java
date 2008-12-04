/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entitystore.foreign;

import java.lang.reflect.Method;
import java.util.HashMap;

public class QueryMethodDescriptor
{
    private Class resultType;
    private Method method;
    private HashMap<String, Integer> parameters;

    public QueryMethodDescriptor( Method method, Class resultType, HashMap<String, Integer> parameters )
    {
        this.method = method;
        this.resultType = resultType;
        this.parameters = parameters;
    }

    public Method method()
    {
        return this.method;
    }

    public HashMap<String, Integer> parameters()
    {
        return this.parameters;
    }

    public Class resultType()
    {
        return resultType;
    }
}