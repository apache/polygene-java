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

package org.qi4j.library.http;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JAVADOC
 */
public class WelcomeServlet
    extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override public void init() throws ServletException
    {
        super.init();
    }

    @Override protected void doGet( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse ) throws ServletException, IOException
    {
        PrintWriter writer = httpServletResponse.getWriter();
        writer.println( "Welcome!" );
    }
}