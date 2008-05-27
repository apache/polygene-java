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

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ISessionSettings;
import static org.apache.wicket.util.lang.Objects.setObjectStreamFactory;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.quikit.assembly.composites.QuikItPageFactoryComposite;
import org.qi4j.quikit.pages.MainPage;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.structure.Module;

/**
 * @author Niclas Hedman
 * @author Nino Martinez Wael (nino.martinez@jayway.dk)
 */
public class QuikItApplication extends WebApplication
{
    @Uses
    private QuikItFilter filter;

    @Structure
    private CompositeBuilderFactory cbf;

    @Structure
    private ObjectBuilderFactory obf;

    // TODO: Remove this once unit of work factory is serializable
    @Deprecated
    @Structure
    private UnitOfWorkFactory uowf;

    // TODO: Remove this once unit of work factory is serializable
    @Deprecated
    @Structure
    private Qi4jSPI qi4jSPI;

    // TODO: Remove this once module is serializable
    @Deprecated
    @Structure
    private Module module;

    @Override
    public final String getConfigurationType()
    {
        return DEPLOYMENT;
    }

    @Override
    public Session newSession( Request request, Response response )
    {
        ObjectBuilder<QuikitSession> sessionBuilder = obf.newObjectBuilder( QuikitSession.class );
        sessionBuilder.use( request, response );
        return sessionBuilder.newInstance();
    }

    @Override
    protected void init()
    {
        super.init();

        ISessionSettings sessionSettings = getSessionSettings();
        IPageFactory pageFactory = cbf.newComposite( QuikItPageFactoryComposite.class );
        sessionSettings.setPageFactory( pageFactory );

        // Sets the object stream factory builder
        Qi4jObjectStreamFactory objectStreamFactory = obf.newObject( Qi4jObjectStreamFactory.class );
        setObjectStreamFactory( objectStreamFactory );
    }

    @Override
    public final Class<MainPage> getHomePage()
    {
        return MainPage.class;
    }

    /**
     * Returns the quikit application.
     *
     * @return The quikit application.
     * @since 0.2.0
     */
    public static QuikItApplication get()
    {
        return (QuikItApplication) WebApplication.get();
    }

    // TODO: Remove this method once composite builder factory is serializable.
    @Deprecated
    final CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return cbf;
    }

    // TODO: Remove this method once object builder factory is serializable.
    @Deprecated
    final ObjectBuilderFactory getObjectBuilderFactory()
    {
        return obf;
    }

    // TODO: Remove this method once unit of work factory is serializable.
    @Deprecated
    final UnitOfWorkFactory getUnitOfWorkFactory()
    {
        return uowf;
    }

    // TODO: Remove this method once qi4jspi is serializable.
    @Deprecated
    final Qi4jSPI getQi4jSPI()
    {
        return qi4jSPI;
    }

    // TODO: Remove this once module is serialziable.
    @Deprecated
    final Module getModule()
    {
        return module;
    }
}
