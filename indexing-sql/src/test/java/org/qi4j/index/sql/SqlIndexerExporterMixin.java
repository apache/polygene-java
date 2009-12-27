/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.index.sql;

import java.io.OutputStream;

/**
 * JAVADOC Add JavaDoc
 */
public abstract class SqlIndexerExporterMixin
    implements SqlIndexerExporterComposite
{
    public void activate()
        throws Exception
    {
    }

    public void passivate()
        throws Exception
    {
    }

    public void toSQL( final OutputStream outputStream )
    {
    }
}