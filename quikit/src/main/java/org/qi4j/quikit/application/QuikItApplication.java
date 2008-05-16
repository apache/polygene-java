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
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ISessionSettings;
import static org.apache.wicket.util.lang.Objects.*;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.Uses;
import org.qi4j.quikit.application.Qi4jObjectStreamFactory;
import org.qi4j.quikit.assembly.composites.QuikItPageFactoryComposite;
import org.qi4j.quikit.pages.MainPage;

/**
 * @author Niclas Hedman
 * @author Nino Martinez Wael (nino.martinez@jayway.dk)
 */
public class QuikItApplication extends WebApplication
{
    @Uses
    private QuikItFilter filter;

    @Structure
    CompositeBuilderFactory factory;

    @Structure
    private ObjectBuilderFactory objectBuilderFactory;

    @Override
    protected void init()
    {
        super.init();

        ISessionSettings sessionSettings = getSessionSettings();
        IPageFactory pageFactory = factory.newComposite( QuikItPageFactoryComposite.class );
        sessionSettings.setPageFactory( pageFactory );

        // Sets the object stream factory builder
        ObjectBuilder<Qi4jObjectStreamFactory> objectStreamFactoryBuilder = objectBuilderFactory
            .newObjectBuilder( Qi4jObjectStreamFactory.class );
        Qi4jObjectStreamFactory objectStreamFactory = objectStreamFactoryBuilder
            .newInstance();
        setObjectStreamFactory( objectStreamFactory );
    }

    @Override
    public Class<MainPage> getHomePage()
    {
        return MainPage.class;
    }
}
