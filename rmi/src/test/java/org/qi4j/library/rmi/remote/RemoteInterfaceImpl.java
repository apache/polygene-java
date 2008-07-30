/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.rmi.remote;

import java.io.IOException;

/**
 * Implementation of RemoteInterface.
 */
public class RemoteInterfaceImpl
    implements RemoteInterface
{
    int count = 0;

    // RemoteInterface implementation --------------------------------
    public String foo( String aBar )
        throws IOException
    {
        count++;

        if( count % 2 == 0 )
        {
            throw new IOException( "Something went wrong" );
        }

        return "Foo:" + aBar;
    }
}
