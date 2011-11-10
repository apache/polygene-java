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
package org.qi4j.library.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Paul Merlin
 */
public class Qi4jVaadinApplicationServlet
        extends AbstractApplicationServlet
{

    @Structure
    private ObjectFactory obf;

    @Override
    protected Application getNewApplication( HttpServletRequest request )
            throws ServletException
    {
        try {
            return obf.newObject( getApplicationClass() );
        } catch ( ClassNotFoundException ex ) {
            throw new ServletException( "getNewApplication failed", ex );
        }
    }

    @Override
    protected Class<? extends Application> getApplicationClass()
            throws ClassNotFoundException
    {
        return Application.class;
    }

}
