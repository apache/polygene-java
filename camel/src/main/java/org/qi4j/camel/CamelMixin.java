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

package org.qi4j.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.qi4j.service.Activatable;

/**
 * TODO
 */
public class CamelMixin
    implements Activatable
{
    private CamelContext context = new DefaultCamelContext();

    public void activate() throws Exception
    {
        context.start();
        ProducerTemplate template = context.createProducerTemplate();

        //smtps://rickardoberg@smtp.gmail.com?password=somepwd]

    }

    public void passivate() throws Exception
    {
        context.stop();
    }
}
