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

package org.apache.polygene.metrics.codahale;

import com.codahale.metrics.health.HealthCheck;
import org.apache.polygene.api.metrics.MetricsHealthCheck;

public class CodahaleHealthCheck
    implements MetricsHealthCheck
{
    private final MetricsHealthCheck check;

    public CodahaleHealthCheck( MetricsHealthCheck check )
    {
        this.check = check;
    }

    @Override
    public Result check()
        throws Exception
    {
        return check.check();
    }

    static Result wrap( HealthCheck.Result result )
    {
        if( result.isHealthy() )
        {
            return Result.healthOk();
        }
        String message = result.getMessage();
        Throwable error = result.getError();
        if( error != null )
        {
            return Result.exception( message, error );
        }
        return Result.unhealthy( message );
    }

    static HealthCheck.Result unwrap( Result result )
    {
        String message = result.getMessage();
        if( result.isHealthy() )
        {
            if( message != null )
            {
                return HealthCheck.Result.healthy( message );
            }
            return HealthCheck.Result.healthy();
        }
        Throwable error = result.getException();
        if( error != null )
        {
            return HealthCheck.Result.unhealthy( error );
        }
        return HealthCheck.Result.unhealthy( message );
    }
}
