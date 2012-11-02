/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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

package org.qi4j.logging.trace.service;

import java.lang.reflect.Method;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;

public interface TraceService
{
    int traceLevel();

    void traceSuccess( Class compositeType, Composite object, Method method, @Optional Object[] args, Object result, long entryTime, long durationNano );

    void traceException( Class compositeType, Composite object, Method method, @Optional Object[] args, Throwable t, long entryTime, long durationNano );

}
