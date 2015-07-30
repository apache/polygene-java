/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.sample.forum.rest;

import org.apache.zest.library.rest.server.api.ContextRestlet;
import org.apache.zest.sample.forum.rest.resource.RootResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;

/**
 * TODO
 */
public class ForumRestlet
    extends ContextRestlet
{
    @Override
    protected Uniform createRoot( Request request, Response response )
    {
        return module.newObject( RootResource.class, this );
    }
}
