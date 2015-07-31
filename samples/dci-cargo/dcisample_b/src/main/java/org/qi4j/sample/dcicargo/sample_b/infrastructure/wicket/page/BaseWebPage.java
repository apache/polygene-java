/*
 * Copyright 2011 Marc Grue.
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
package org.qi4j.sample.dcicargo.sample_b.infrastructure.wicket.page;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseWebPage
 *
 * Convenience base page to provide access to common resources
 */
public class BaseWebPage extends WebPage
{
    public Logger logger = LoggerFactory.getLogger( getClass() );

    static protected TransientBuilderFactory tbf;

    public BaseWebPage( PageParameters pageParameters )
    {
        super( pageParameters );
    }

    public BaseWebPage()
    {
    }

    public static <T extends TransientComposite> T query( Class<T> queryClass )
    {
        return tbf.newTransient( queryClass );
    }

    public static void prepareBaseWebPageClass( TransientBuilderFactory transientBuilderFactory )
    {
        tbf = transientBuilderFactory;
    }
}