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
package org.apache.zest.manual.recipes.sideeffects;

import java.lang.reflect.Method;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.sideeffect.GenericSideEffect;

// START SNIPPET: appliesTo
@AppliesTo( { MyAnnotation.class, MyAppliesToFilter.class } )
// START SNIPPET: body
public class MyGenericSideEffect extends GenericSideEffect
{
// END SNIPPET: appliesTo
    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        // Do whatever you need...

        try
        {
            // It is possible to obtain the returned values by using 'result' member;
            Object returnedValue = result.invoke( proxy, method, args );
        } catch( NumberFormatException e )
        {
            // And Exception will be thrown accordingly, in case you need to know.
            throw new IllegalArgumentException(); // But any thrown exceptions are ignored.
        }
        return 23; // Return values will also be ignored.
    }
// START SNIPPET: appliesTo
}
// END SNIPPET: appliesTo
// END SNIPPET: body
