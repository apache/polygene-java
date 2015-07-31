/*
 * Copyright 2009-2010 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.application.factory;

import java.lang.reflect.Method;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;

/**
 * Filter for Event methods. Event methods
 * have ApplicationEvent as their first method parameter.
 */
public class ApplicationEventMethodFilter
        implements AppliesToFilter
{
    @Override
    public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> fragmentClass )
    {
        return method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals( ApplicationEvent.class );
    }
}
