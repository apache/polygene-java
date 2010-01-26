/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.rest;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.servlet.ServerServlet;

/**
 * Integration with Qi4j. Register an object extending Application to use.
 */
public class Qi4jServerServlet
    extends ServerServlet
{
    @Structure
    ObjectBuilderFactory obf;

    @Override
    @SuppressWarnings( "unchecked" )
    protected Application createApplication( Context context )
    {
        ObjectBuilder<Application> app = obf.newObjectBuilder( Application.class );

        app.use( context.createChildContext(), getServletConfig(), getServletContext() );

        return app.newInstance();
    }
}
