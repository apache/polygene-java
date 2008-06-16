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
import org.qi4j.quikit.application.Qi4jObjectStreamFactory;
import org.qi4j.quikit.application.QuikItApplication;
import org.qi4j.quikit.application.QuikItFilter;
import org.qi4j.quikit.application.QuikItServlet;
import org.qi4j.quikit.application.QuikitSession;
import org.qi4j.quikit.application.ServletInfo;
import org.qi4j.quikit.application.ServletService;
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
import org.qi4j.quikit.panels.EntityTypeListViewPanel;
import org.qi4j.quikit.panels.entityList.EntityListViewPanelAssembler;

/**
 * TODO
 */
public class QuikitAssembler implements Assembler
{
    private String jettyIdentity;

    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.addComposites( QuikItPageFactoryComposite.class );
        aModule.addComposites( QuikItServletProviderComposite.class );
        aModule.addComposites( QuikItApplicationFactoryComposite.class );

        // Add jetty service
        aModule.addServices( JettyService.class ).instantiateOnStartup()
            .identifiedBy( jettyIdentity( aModule ) );

        // Add the quikit servlet
        aModule.addServices( ServletService.class )
            .providedBy( QuikItServletProviderComposite.class )
            .setMetaInfo( new ServletInfo( "/quikit/*" ) );

        // Register wicket applications
        aModule.addObjects( QuikItApplication.class );
        aModule.addObjects( QuikitSession.class );
        aModule.addObjects( Qi4jObjectStreamFactory.class );

        aModule.addObjects( QuikItFilter.class );
        aModule.addObjects( QuikItServlet.class );

        // Register Pages
        aModule.addObjects( MainPage.class );
        aModule.addObjects( EntityFormEditPage.class );
        aModule.addObjects( EntityFormViewPage.class );
        aModule.addObjects( EntityListViewPage.class );

        // Registers Panels
        aModule.addObjects( EntityTypeListViewPanel.class );
        aModule.addObjects( EntityFormViewPanel.class );
        aModule.addObjects( EntityFormEditPanel.class );

        aModule.addAssembler( new EntityListViewPanelAssembler() );
    }

    public String jettyIdentity()
    {
        return jettyIdentity;
    }

    private String jettyIdentity( ModuleAssembly module )
    {
        String moduleName = module.name();
        jettyIdentity = moduleName + ":" + JettyService.class.getName();
        return jettyIdentity;
    }
}