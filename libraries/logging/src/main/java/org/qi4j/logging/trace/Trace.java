/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.logging.trace;

import java.lang.annotation.*;
import org.qi4j.api.injection.InjectionScope;

/**
 * The Trace annotation is to indicate which methods should be traced when tracing is enabled.
 * <p/>
 * The <i>level</i> of the Trace indicates the <i>threshold level</i> that the <code>Tracer</code> instance must be set
 * to, to enable tracing. If the <i>threshold level</i> of the <code>Tracer</code> instance is equal to or higher than
 * the <i>level</i> of the <code>Trace</code> annotation the method will be traced.
 * </p>
 * <p/>
 * The <code>Trace</code> annotation will only be used for <code>TraceConcern</code> and not if the
 * all encompassing <code>TraceAllConcern</code> is used.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD } )
@Documented
@InjectionScope
public @interface Trace
{
    int ALL = Integer.MIN_VALUE;
    int LOWLOW = -100;
    int LOW = -50;
    int NORMAL = 0;
    int HIGH = 50;
    int HIGHHIGH = 100;
    int OFF = Integer.MAX_VALUE;

    /**
     * The value is the threshold level required to enable the tracing.
     * <p/>
     * If the Trace level is set to 100 (default), it is required that the
     * Tracer (retrieved from the LogService) used is set to 100 or higher.
     * </p>
     *
     * @return the Trace Level of this Trace annotation.
     */
    int level() default NORMAL;
}
