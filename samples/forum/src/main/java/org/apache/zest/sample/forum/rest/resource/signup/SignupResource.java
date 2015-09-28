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
package org.apache.zest.sample.forum.rest.resource.signup;

import org.apache.zest.library.rest.server.api.ContextResource;
import org.apache.zest.sample.forum.context.signup.Registration;
import org.apache.zest.sample.forum.context.signup.Signup;
import org.restlet.data.Form;

/**
 * TODO
 */
public class SignupResource
    extends ContextResource
{
    public void signup( Registration registration )
    {
        context( Signup.class ).signup( registration );
    }

    public Form signup()
    {
        Form form = new Form();
        form.set( "name", "Rickard" );
        form.set( "realName", "Rickard Öberg" );
        form.set( "password", "rickard" );
        form.set( "email", "rickard@zest" );
        return form;
    }
}
