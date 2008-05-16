/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
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
package org.qi4j.quikit.assembly;

import javax.servlet.Servlet;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.quikit.QueryMetaDataProvider;
import org.qi4j.quikit.application.Qi4jObjectStreamFactory;
import org.qi4j.quikit.application.QuikItApplication;
import org.qi4j.quikit.application.QuikItFilter;
import org.qi4j.quikit.application.QuikItServlet;
import org.qi4j.quikit.application.ServletInfo;
import org.qi4j.quikit.application.jetty.JettyService;
import org.qi4j.quikit.assembly.composites.QuikItApplicationFactoryComposite;
import org.qi4j.quikit.assembly.composites.QuikItPageFactoryComposite;
import org.qi4j.quikit.assembly.composites.QuikItServletProviderComposite;
import org.qi4j.quikit.pages.EntityFormEditPage;
import org.qi4j.quikit.pages.EntityFormViewPage;
import org.qi4j.quikit.pages.EntityListViewPage;
import org.qi4j.quikit.pages.MainPage;
import org.qi4j.quikit.panels.EntityFormEditPanel;
import org.qi4j.quikit.panels.EntityFormViewPanel;
import org.qi4j.quikit.panels.EntityListViewPanel;
import org.qi4j.quikit.panels.EntityTypeListViewPanel;

/**
 * TODO
 */
public class QuikitAssembler implements Assembler
{
    private String jettyIdentity;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addComposites( QuikItPageFactoryComposite.class );
        module.addComposites( QuikItServletProviderComposite.class );
        module.addComposites( QuikItApplicationFactoryComposite.class );

        // Add jetty service
        module.addServices( JettyService.class ).instantiateOnStartup()
            .identifiedBy( jettyIdentity( module ) );

        // Add the quikit servlet
        module.addServices( Servlet.class )
            .providedBy( QuikItServletProviderComposite.class )
            .setServiceAttribute( ServletInfo.class, new ServletInfo( "/quikit/" ) );

        // Register wicket applications
        module.addObjects( QuikItApplication.class );
        module.addObjects( Qi4jObjectStreamFactory.class );

        module.addObjects( QuikItFilter.class );
        module.addObjects( QuikItServlet.class );

        // Register Pages
        module.addObjects( MainPage.class );
        module.addObjects( EntityFormEditPage.class );
        module.addObjects( EntityFormViewPage.class );
        module.addObjects( EntityListViewPage.class );

        // Register helpers
        module.addObjects( QueryMetaDataProvider.class );

        // Registers Panels
        module.addObjects( EntityTypeListViewPanel.class );
        module.addObjects( EntityListViewPanel.class );
        module.addObjects( EntityFormViewPanel.class );
        module.addObjects( EntityFormEditPanel.class );
    }

    public String jettyIdentity()
    {
        return jettyIdentity;
    }

    private String jettyIdentity( ModuleAssembly module )
    {
        String moduleName = module.getName();
        jettyIdentity = moduleName + ":" + JettyService.class.getName();
        return jettyIdentity;
    }
}