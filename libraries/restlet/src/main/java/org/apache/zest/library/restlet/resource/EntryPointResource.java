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

package org.apache.zest.library.restlet.resource;

import java.util.HashMap;
import java.util.Map;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.library.restlet.RestLink;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.routing.Route;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

@Mixins( EntryPointResource.Mixin.class )
public interface EntryPointResource extends ServerResource<EntryPoint>
{
    abstract class Mixin
        implements EntryPointResource
    {
        @This
        private Parameters<EntryPoint> parameters;

        @Service
        private ResourceBuilder resourceBuilder;

        @Structure
        private ValueBuilderFactory vbf;

        private EntryPoint entryPoint;

        @Override
        public EntryPoint get()
        {
            if( entryPoint == null )
            {
                entryPoint = createEntryPoint();
            }
            return entryPoint;
        }

        private EntryPoint createEntryPoint()
        {
            Map<String, RestLink> entryPoints = new HashMap<>();
            for( Route r : parameters.router().get().getRoutes() )
            {
                if( r instanceof TemplateRoute)
                {
                    TemplateRoute route = (TemplateRoute) r;
                    Template template = route.getTemplate();
                    // Only include patterns that doesn't have variables, and has a proper name.
                    if( template.getVariableNames().isEmpty() && route.getName().indexOf( '>' ) == -1 )
                    {
                        Reference hostRef = parameters.request().get().getOriginalRef();
//                        Reference reference = new Reference( hostRef, template.getPattern() );
                        RestLink link;
                        if( route.getDescription() == null )
                        {
                            link = resourceBuilder.createRestLink( template.getPattern() , hostRef, Method.GET );
                        }
                        else
                        {
                            link = resourceBuilder.createRestLink( template.getPattern() , hostRef, Method.GET, route.getDescription() );
                        }
                        entryPoints.put( route.getName(), link );
                    }
                }
            }
            ValueBuilder<EntryPoint> builder = vbf.newValueBuilder( EntryPoint.class );
            builder.prototype().identity().set( "/" );
            builder.prototype().api().set( entryPoints );
            return builder.newInstance();
        }
    }
}