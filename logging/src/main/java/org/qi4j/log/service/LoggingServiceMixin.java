/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.log.service;

import org.qi4j.composite.Composite;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.injection.scope.Structure;
import org.qi4j.log.service.LoggingService;
import org.qi4j.log.LogType;

public abstract class LoggingServiceMixin
    implements LoggingService
{
    @Structure private UnitOfWorkFactory unitOfWorkFactory;

    public void log( LogType type, Composite composite, String category, String message )
    {

    }

    public void log( LogType type, Composite composite, String category, String message, Object param1 )
    {

    }

    public void log( LogType type, Composite composite, String category, String message, Object param1, Object param2 )
    {

    }

    public void log( LogType type, Composite composite, String category, String message, Object... params )
    {

    }

}
