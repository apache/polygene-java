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
package org.apache.zest.library.sql.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.library.circuitbreaker.CircuitBreaker;

import static org.apache.zest.library.circuitbreaker.CircuitBreakers.in;
import static org.apache.zest.library.circuitbreaker.CircuitBreakers.rootCause;

public class DataSources
{

    public static CircuitBreaker newDataSourceCircuitBreaker()
    {
        return newDataSourceCircuitBreaker( 5, 1000 * 60 * 5 );
    }

    public static CircuitBreaker newDataSourceCircuitBreaker( int threshold, long timeout )
    {
        @SuppressWarnings( "unchecked" )
        Predicate<Throwable> in = in( ConnectException.class );
        return new CircuitBreaker( threshold, timeout, rootCause( in ).negate() );
    }

    public static DataSource wrapWithCircuitBreaker(final Identity dataSourceIdentity, final DataSource pool, final CircuitBreaker circuitBreaker )
    {
        // Create wrapper
        InvocationHandler handler = new InvocationHandler()
        {
            @Override
            public Object invoke( Object proxy, Method method, Object[] args )
                    throws Throwable
            {
                if ( !circuitBreaker.isOn() ) {
                    Throwable throwable = circuitBreaker.lastThrowable();
                    if ( throwable != null ) {
                        throw throwable;
                    } else {
                        throw new ServiceImporterException( "Circuit breaker for DataSource " + dataSourceIdentity + " is not on" );
                    }
                }

                try {
                    Object result = method.invoke( pool, args );
                    circuitBreaker.success();
                    return result;
                } catch ( IllegalAccessException e ) {
                    circuitBreaker.throwable( e );
                    throw e;
                } catch ( IllegalArgumentException e ) {
                    circuitBreaker.throwable( e );
                    throw e;
                } catch ( InvocationTargetException e ) {
                    circuitBreaker.throwable( e.getCause() );
                    throw e.getCause();
                }
            }

        };

        // Create proxy with circuit breaker
        return ( DataSource ) Proxy.newProxyInstance( DataSource.class.getClassLoader(), new Class[]{ DataSource.class }, handler );
    }

    private DataSources()
    {
    }

}
