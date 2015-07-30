/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.io.docsupport;

import java.io.File;
import java.io.IOException;
import org.apache.zest.io.Inputs;
import org.apache.zest.io.Outputs;

// START SNIPPET: io2
import org.apache.zest.io.Transforms.Counter;
import static org.apache.zest.io.Transforms.map;
// END SNIPPET: io2

public class IoDocs
{
    public static void main( String[] args )
        throws IOException
    {
        {
// START SNIPPET: io1
            File source = new File( "source.txt" );
            File destination = new File( "destination.txt" );
            Inputs.text( source ).transferTo( Outputs.text( destination ) );
// END SNIPPET: io1
        }
        {
// START SNIPPET: io2
            File source = new File( "source.txt" );
            File destination = new File( "destination.txt" );
            Counter<String> counter = new Counter<String>();
            Inputs.text( source ).transferTo( map(counter, Outputs.text(destination) ));
            System.out.println( "Lines: " + counter.count() );
// END SNIPPET: io2
        }
    }
}
