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

package org.apache.polygene.api.service.qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Predicate;
import org.apache.polygene.api.service.ServiceReference;

/**
 * Filter services based on whether they are available or not.
 *
 * At an injection point you can do this:
 *
 * <pre><code>
 * &#64;Service &#64;Available MyService service;
 * </code></pre>
 * to get only a service that is currently available.
 */
@Retention( RetentionPolicy.RUNTIME )
@Qualifier( Available.AvailableQualifier.class )
public @interface Available
{
    /**
     * Available Annotation Qualifier.
     * See {@link Available}.
     */
    public final class AvailableQualifier
        implements AnnotationQualifier<Available>
    {
        @Override
        public <T> Predicate<ServiceReference<?>> qualifier( Available active )
        {
            return ServiceQualifier.whereAvailable();
        }
    }
}
