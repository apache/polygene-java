/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.mixin.decoratorMixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FooModelInvocationHandler
    implements InvocationHandler
{
    private String value;

    public FooModelInvocationHandler( String value )
    {
        this.value = value;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if(method.getName().equals( "hashCode" ))
            return hashCode();
        if(method.getName().equals( "equals" ))
            return equals(args[0]);
        if(args==null || args.length==0)
            return value;
        value = (String) args[0];
        return null;
    }
}
