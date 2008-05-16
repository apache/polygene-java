/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.quikit.application;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.quikit.assembly.composites.QuikItApplicationFactoryComposite;

public class QuikItFilter extends WicketFilter
{
    private IWebApplicationFactory applicationFactory;

    public QuikItFilter( @Structure CompositeBuilderFactory factory )
    {
        applicationFactory = factory.newComposite( QuikItApplicationFactoryComposite.class );
    }

    @Override
    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
        throws IOException, ServletException
    {
        super.doFilter( servletRequest, servletResponse, filterChain );
    }

    @Override
    public boolean doGet( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse )
        throws ServletException, IOException
    {
        return super.doGet( httpServletRequest, httpServletResponse );
    }

    @Override
    protected IWebApplicationFactory getApplicationFactory()
    {
        return applicationFactory;
    }
}
