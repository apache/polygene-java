/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.entitystore.prefs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ListPreferencesNodes
{
    public static void main( String[] args )
        throws Exception
    {
        Preferences preferences = Preferences.userRoot();
        printNode( preferences, "" );
    }

    private static void printNode( Preferences node, String indent )
        throws BackingStoreException
    {
        System.out.print( indent );
        String name = node.name();
        if( "".equals( name ) )
        {
            name = "/";
        }

        System.out.print( name );
        String[] nodes = node.keys();
        if( nodes.length > 0 )
        {
            System.out.print( "  { " );
            boolean first = true;
            for( String key : nodes )
            {
                if( !first )
                {
                    System.out.print( ", " );
                }
                first = false;
                System.out.print( key );
            }
            System.out.print( " }" );
        }
        System.out.println();
        for( String childName : node.childrenNames() )
        {
            Preferences child = node.node( childName );
            printNode( child, indent + "  " );
        }
    }
}
