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
package org.qi4j.quikit.application.mixins;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.composite.scope.Structure;
import org.qi4j.quikit.application.QuikItApplication;

public class QuikItApplicationFactoryMixin
    implements IWebApplicationFactory
{
    @Structure private ObjectBuilderFactory objectBuilderFactory;

    public WebApplication createApplication( WicketFilter wicketFilter )
    {
        ObjectBuilder<QuikItApplication> builder = objectBuilderFactory.newObjectBuilder( QuikItApplication.class );
        builder.use( wicketFilter );
        return builder.newInstance();
    }
}
