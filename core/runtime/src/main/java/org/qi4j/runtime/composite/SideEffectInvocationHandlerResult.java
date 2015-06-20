/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.runtime.composite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public final class SideEffectInvocationHandlerResult
    implements InvocationHandler
{
    private Object result;
    private Throwable throwable;

    public SideEffectInvocationHandlerResult()
    {
    }

    public void setResult( Object result, Throwable throwable )
    {
        this.result = result;
        this.throwable = throwable;
    }

    // InvocationHandler implementation ------------------------------

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        if( throwable != null )
        {
            throw throwable;
        }
        else
        {
            return result;
        }
    }
}
