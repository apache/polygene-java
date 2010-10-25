/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.service.qualifier;

/**
 * Filter services based on whether they are active or not.
 *
 * At an injection point you can do this:
 * @Service @Active MyService service;
 * to get only a service that is currently active.
 */
@Qualifier( Active.ActiveQualifier.class )
public @interface Active
{
    public final class ActiveQualifier
        implements AnnotationQualifier<Active>
    {
        public <T> ServiceQualifier qualifier( Active active )
        {
            return ServiceQualifier.whereActive();
        }
    }
}
