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
package org.qi4j.sample.dcicargo.pathfinder_a.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GraphDAO
{

    private static final Random random = new Random();

    public List<String> listLocations()
    {
        return new ArrayList<String>( Arrays.asList(
            "CNHKG", "AUMEL", "SESTO", "FIHEL", "USCHI", "JNTKO", "DEHAM", "CNSHA", "NLRTM", "SEGOT", "CNHGH", "USNYC", "USDAL"
        ) );
    }

    public String getVoyageNumber( String from, String to )
    {
        final int i = random.nextInt( 5 );
        if( i == 0 )
        {
            return "V100S";
        }
        if( i == 1 )
        {
            return "V200T";
        }
        if( i == 2 )
        {
            return "V300A";
        }
        if( i == 3 )
        {
            return "V400S";
        }
        return "V500S";
    }
}
