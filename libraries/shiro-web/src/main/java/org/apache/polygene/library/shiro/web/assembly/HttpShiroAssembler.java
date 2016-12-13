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
package org.apache.polygene.library.shiro.web.assembly;

import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.shiro.ini.ShiroIniConfiguration;
import org.apache.polygene.library.shiro.web.EnvironmentLoaderService;
import org.apache.polygene.library.shiro.web.ShiroFilterService;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.INCLUDE;
import static javax.servlet.DispatcherType.REQUEST;
import static org.apache.polygene.library.http.Servlets.addContextListeners;
import static org.apache.polygene.library.http.Servlets.addFilters;
import static org.apache.polygene.library.http.Servlets.filter;
import static org.apache.polygene.library.http.Servlets.listen;

public class HttpShiroAssembler
    extends Assemblers.Config<HttpShiroAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        addContextListeners( listen().
            with( EnvironmentLoaderService.class ) ).
            to( module );

        addFilters( filter( "/*" ).
            through( ShiroFilterService.class ).
            on( REQUEST, FORWARD, INCLUDE, ERROR, ASYNC ) ).
            to( module );

        if( hasConfig() )
        {
            configModule().entities( ShiroIniConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }
}
