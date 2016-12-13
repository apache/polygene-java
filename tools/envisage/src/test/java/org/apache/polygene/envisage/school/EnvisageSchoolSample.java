/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.envisage.school;

import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.bootstrap.Energy4Java;
import org.apache.polygene.envisage.Envisage;

public class EnvisageSchoolSample
{
    // START SNIPPET: envisage
    public static void main( String[] args )
        throws Exception
    {
        Energy4Java energy4Java = new Energy4Java();

        ApplicationDescriptor applicationModel
                              = energy4Java.newApplicationModel( new SchoolAssembler() );

        new Envisage().run( applicationModel );
    }
    // END SNIPPET: envisage
}
