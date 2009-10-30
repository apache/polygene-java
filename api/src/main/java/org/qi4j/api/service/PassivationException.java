/*
 * Copyright 2009 Niclas Hedhman.
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

package org.qi4j.api.service;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class PassivationException
    extends Exception
{
    private Exception[] causes;

    public PassivationException( List<Exception> exceptions )
    {
        causes = new Exception[exceptions.size()];
        exceptions.toArray( causes );
    }

    public Exception[] causes()
    {
        return causes;
    }

    public void printStackTrace( PrintStream stream )
    {
        synchronized( stream )
        {
            int counter = 1;
            super.printStackTrace( stream );
            for( Exception exc : causes )
            {
                stream.print( "Cause " + counter++ + " : " );
                exc.printStackTrace( stream );
            }
        }
    }

    public void printStackTrace( PrintWriter writer )
    {
        synchronized( writer )
        {
            int counter = 1;
            super.printStackTrace( writer );
            for( Exception exc : causes )
            {
                writer.print( "Cause " + counter++ + " : " );
                exc.printStackTrace( writer );
            }
        }
    }
}
