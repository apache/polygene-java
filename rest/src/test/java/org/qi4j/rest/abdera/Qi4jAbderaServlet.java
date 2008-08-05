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

package org.qi4j.rest.abdera;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.impl.DefaultProvider;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.qi4j.injection.scope.Structure;
import org.qi4j.object.ObjectBuilderFactory;

/**
 * TODO
 */
public class Qi4jAbderaServlet
    extends AbderaServlet
{
    @Structure ObjectBuilderFactory obf;

    @Override protected Provider createProvider()
    {
        Qi4jEntityAdapter entityAdapter = obf.newObject( Qi4jEntityAdapter.class );
        entityAdapter.setHref( "entity" );

        SimpleWorkspaceInfo wi = new SimpleWorkspaceInfo();
        wi.setTitle( "Entity Directory Workspace" );
        wi.addCollection( entityAdapter );

        DefaultProvider provider = new DefaultProvider( "/" );
        provider.addWorkspace( wi );

        provider.init( getAbdera(), null );
        return provider;
    }

    @Override public void init() throws ServletException
    {
        super.init();
    }

    @Override protected void service( HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse ) throws ServletException, IOException
    {
        super.service( httpServletRequest, httpServletResponse );
    }
}