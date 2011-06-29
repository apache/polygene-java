/*  Copyright 2009 Alex Shneyderman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.qrm.internal;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class QrmHibernateConfiguration
    extends Configuration
{

    private String descriptor;

    public QrmHibernateConfiguration( String descriptor )
    {
        this.descriptor = descriptor;
    }

    protected InputStream getConfigurationInputStream( String resource )
        throws HibernateException
    {
        return new ByteArrayInputStream( descriptor.getBytes() );
    }
}
