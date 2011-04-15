/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.samples.dddsample.domain.model;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author edward.yakop@gmail.com
 */
public class SampleConstants
{
    private static final Timestamp base;

    static
    {
        try
        {
            Date date = new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2008-01-01" );
            base = new Timestamp( date.getTime() - 1000L * 60 * 60 * 24 * 100 );
        }
        catch( ParseException e )
        {
            throw new RuntimeException( e );
        }
    }

    private SampleConstants()
    {
    }

    private static Timestamp ts( int hours )
    {
        return new Timestamp( base.getTime() + 1000L * 60 * 60 * hours );
    }

    public static Date offset( int hours )
    {
        return new Date( ts( hours ).getTime() );
    }
}
