/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.qi4j.api.structure.Application;
import org.qi4j.library.servlet.lifecycle.AbstractQi4jServletBootstrap;

/**
 * Base HttpServlet providing easy access to the {@link org.qi4j.api.structure.Application} from the
 * {@link javax.servlet.ServletContext}.
 *
 * @see AbstractQi4jServletBootstrap
 */
public class Qi4jServlet
        extends HttpServlet
{

    private Application application;

    public Qi4jServlet()
    {
        super();
    }

    @Override
    public void init()
            throws ServletException
    {
        super.init();
        application = Qi4jServletSupport.application( getServletContext() );
    }

    protected final Application application()
    {
        return application;
    }

}
