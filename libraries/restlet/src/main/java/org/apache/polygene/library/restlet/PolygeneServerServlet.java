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
package org.apache.polygene.library.restlet;

import java.util.List;
import javax.servlet.Servlet;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.structure.Module;
import org.restlet.Context;
import org.restlet.ext.servlet.ServerServlet;
import org.restlet.routing.VirtualHost;
import org.restlet.util.ServerList;

/**
 * Restlet ServerServlet backed by a org.restlet.Application object.
 */
@Mixins( PolygeneServerServlet.Mixin.class )
public interface PolygeneServerServlet extends Servlet
{
    class Mixin extends ServerServlet
    {
        @Structure
        private ObjectFactory objectFactory;

        @Override
        protected org.restlet.Application createApplication( Context parentContext )
        {
            return objectFactory.newObject( PolygeneRestApplication.class, parentContext.createChildContext() );
        }
    }
}
