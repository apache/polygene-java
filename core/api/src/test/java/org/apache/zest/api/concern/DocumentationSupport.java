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
package org.apache.zest.api.concern;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.zest.api.common.AppliesTo;
import org.apache.zest.api.common.AppliesToFilter;
import org.apache.zest.api.injection.InjectionScope;

public class DocumentationSupport
{
// START SNIPPET: class
    @AppliesTo( java.sql.Connection.class )
    public class CacheConcern extends GenericConcern
        implements InvocationHandler
    {
// END SNIPPET: class
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: filter
    @AppliesTo( BusinessAppliesToFilter.class )
    public class BusinessConcern extends GenericConcern
        implements InvocationHandler
    {
// END SNIPPET: filter
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: filter
    public class BusinessAppliesToFilter
        implements AppliesToFilter
    {

        @Override
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass
        )
        {
            return true; // Some criteria for when a method is wrapped with the concern.
        }
    }
// END SNIPPET: filter


// START SNIPPET: annotation
    @AppliesTo( Audited.class )
    public class AuditConcern extends GenericConcern
        implements InvocationHandler
    {
// START SNIPPET: annotation
        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            return null;
        }
    }

// START SNIPPET: annotation
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD } )
    @Documented
    @InjectionScope
    public @interface Audited
    {
    }
// END SNIPPET: annotation
}
