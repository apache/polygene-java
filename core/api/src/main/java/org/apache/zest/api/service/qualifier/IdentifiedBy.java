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

package org.apache.zest.api.service.qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Predicate;
import org.apache.zest.api.service.ServiceReference;

/**
 * Filter services based on identity. Identity can be set during assembly, like so:
 * <pre><code>
 * module.addService(MyService.class).identifiedBy("myservice1");
 * </code></pre>
 *
 * and then at an injection point you can do this:
 * <pre><code>
 * &#64;Service @IdentifiedBy("myservice1") MyService service;
 * </code></pre>
 * to get only a service identified "myservice1".
 */
@Retention( RetentionPolicy.RUNTIME )
@Qualifier( IdentifiedBy.IdentifiedByQualifier.class )
public @interface IdentifiedBy
{
    public abstract String value();

    /**
     * IdentifiedBy Annotation Qualifier.
     * See {@link IdentifiedBy}.
     */
    public final class IdentifiedByQualifier
        implements AnnotationQualifier<IdentifiedBy>
    {
        @Override
        public <T> Predicate<ServiceReference<?>> qualifier( IdentifiedBy identifiedBy )
        {
            return ServiceQualifier.withId( identifiedBy.value() );
        }
    }
}
