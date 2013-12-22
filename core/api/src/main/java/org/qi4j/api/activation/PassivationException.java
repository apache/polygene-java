/*
 * Copyright 2009 Niclas Hedhman.
 * Copyright 2013 Paul Merlin.
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
package org.qi4j.api.activation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Thrown when unable to passivate.
 *
 * Printed StackTrace contains all causes in order as suppressed exceptions.
 */
public final class PassivationException
    extends Exception
{

    private static final long serialVersionUID = 1L;
    private final List<Exception> causes;

    /**
     * Create new PassivationException.
     * @param exceptions All exceptions encountered during passivation, in order
     */
    public PassivationException( Collection<Exception> exceptions )
    {
        super( "Passivation Exception - [has " + exceptions.size() + " cause(s)]" );
        for( Throwable cause : exceptions )
        {
            addSuppressed( cause );
        }
        this.causes = new ArrayList<>( exceptions );
    }

    /**
     * @return All exceptions encountered during passivation, in order
     */
    public List<Exception> causes()
    {
        return causes;
    }

}
