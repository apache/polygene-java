/*
 * Copyright 2008 Richard Wallace.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.http;

import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import static org.qi4j.api.common.Visibility.layer;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.http.Dispatchers.Dispatcher;

public final class Servlets
{
    private Servlets()
    {
    }

    public static ServletDeclaration serve( String path )
    {
        return new ServletDeclaration( path );
    }

    public static ServletAssembler addServlets( ServletDeclaration... servletDeclarations )
    {
        return new ServletAssembler( servletDeclarations );
    }

    public static class ServletAssembler
    {
        final ServletDeclaration[] servletDeclarations;

        ServletAssembler( ServletDeclaration... servletDeclarations )
        {
            this.servletDeclarations = servletDeclarations;
        }

        @SuppressWarnings( "unchecked" )
        public void to( ModuleAssembly module ) throws AssemblyException
        {
            for( ServletDeclaration servletDeclaration : servletDeclarations )
            {
                module.addServices( servletDeclaration.servlet() )
                    .setMetaInfo( servletDeclaration.servletInfo() )
                    .instantiateOnStartup()
                    .visibleIn( layer );
            }
        }
    }

    public static class ServletDeclaration
    {
        String path;
        Class<? extends ServiceComposite> servlet;
        Map<String, String> initParams;

        ServletDeclaration( String path )
        {
            this.path = path;
        }

        public <T extends Servlet & ServiceComposite> ServletDeclaration with( Class<T> servlet )
        {
            this.servlet = servlet;
            return this;
        }

        public ServletDeclaration withInitParams( Map<String, String> initParams )
        {
            this.initParams = initParams;
            return this;
        }

        Class<? extends ServiceComposite> servlet()
        {
            return servlet;
        }

        ServletInfo servletInfo()
        {
            return new ServletInfo( path, initParams );
        }
    }

    public static FilterAssembler filter( String path )
    {
        return new FilterAssembler( path );
    }

    public static FilterDeclaration addFilters( FilterAssembler... filterAssemblers )
    {
        return new FilterDeclaration( filterAssemblers );
    }

    public static class FilterDeclaration
    {
        final FilterAssembler[] filterAssemblers;

        FilterDeclaration( FilterAssembler... filterAssemblers )
        {
            this.filterAssemblers = filterAssemblers;
        }

        @SuppressWarnings( "unchecked" )
        public void to( ModuleAssembly module ) throws AssemblyException
        {
            for( FilterAssembler filterAssembler : filterAssemblers )
            {
                module.addServices( filterAssembler.filter() ).setMetaInfo(
                    filterAssembler.filterInfo() ).instantiateOnStartup()
                    .visibleIn( layer );
            }
        }

    }

    public static class FilterAssembler
    {
        String path;
        Class<? extends ServiceComposite> filter;
        Dispatchers dispatchers;
        Map<String, String> initParams;

        FilterAssembler( String path )
        {
            this.path = path;
        }

        public <T extends Filter & ServiceComposite> FilterAssembler through(
            Class<T> filter )
        {
            this.filter = filter;
            return this;
        }

        public FilterAssembler on( Dispatcher first, Dispatcher... rest )
        {
            dispatchers = Dispatchers.dispatchers( first, rest );
            return this;
        }

        public FilterAssembler withInitParams( Map<String, String> initParams )
        {
            this.initParams = initParams;
            return this;
        }

        Class<? extends ServiceComposite> filter()
        {
            return filter;
        }

        FilterInfo filterInfo()
        {
            return new FilterInfo( path, initParams, dispatchers );
        }
    }

}
