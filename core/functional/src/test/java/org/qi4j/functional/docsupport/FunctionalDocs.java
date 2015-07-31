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
package org.qi4j.functional.docsupport;

import java.util.ArrayList;

// START SNIPPET: func2
import static org.qi4j.functional.ForEach.forEach;
import static org.qi4j.functional.Functions.longSum;
// END SNIPPET: func2

public class FunctionalDocs
{
    public static void main( String[] args )
    {
        {
// START SNIPPET: func1
            Iterable<Long> data = new ArrayList<Long>();
// END SNIPPET: func1
// START SNIPPET: func1

            long sum = 0;
            for( Long point : data )
            {
                sum = sum + point;
            }
            System.out.println( "The sum is " + sum );
// END SNIPPET: func1
        }
        {
// START SNIPPET: func2
            Iterable<Number> data = new ArrayList<Number>();
            Long sum = forEach( data ).map( longSum() ).last();
            System.out.println( "The sum is " + sum );

// END SNIPPET: func2
        }
    }
}
