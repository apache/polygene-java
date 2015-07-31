/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.metrics.yammer;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import org.qi4j.api.metrics.MetricsHealthCheck;

public class YammerHealthCheck
    implements MetricsHealthCheck
{
    private MetricsHealthCheck check;

    public YammerHealthCheck( Class<?> origin, String name, final MetricsHealthCheck check )
    {
        this.check = check;
        HealthChecks.register( new HealthCheck( name )
        {
            @Override
            protected Result check()
                throws Exception
            {
                MetricsHealthCheck.Result result = check.check();
                String message = result.getMessage();
                if( result.isHealthy() )
                {
                    if( message != null )
                    {
                        return Result.healthy( message );
                    }
                    return Result.healthy();
                }
                Throwable exception = result.getException();
                if( exception != null )
                {
                    return Result.unhealthy( exception );
                }
                return Result.unhealthy( message );
            }
        } );
    }

    @Override
    public Result check()
        throws Exception
    {
        return check.check();
    }
}
