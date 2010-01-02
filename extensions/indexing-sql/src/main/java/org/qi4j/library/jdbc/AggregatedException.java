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

package org.qi4j.library.jdbc;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class AggregatedException
    extends Exception
{
    private List<Exception> exceptions;

    public AggregatedException( List<Exception> exceptions )
    {
        this.exceptions = exceptions;
    }

    @Override
    public void printStackTrace( PrintStream s )
    {
        super.printStackTrace(s);
        for( Exception e : exceptions )
        {
            e.printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace( PrintWriter s )
    {
        super.printStackTrace(s);
        for( Exception e : exceptions )
        {
            e.printStackTrace(s);
        }
    }
}
