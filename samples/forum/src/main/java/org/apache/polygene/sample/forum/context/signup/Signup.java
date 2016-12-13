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
package org.apache.polygene.sample.forum.context.signup;

import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.sample.forum.context.Context;
import org.apache.polygene.sample.forum.context.Events;

/**
 * TODO
 */
public class Signup
    extends Context
{
    @Uses
    Users users;

    @Service
    Events events;

    public void signup( Registration registration )
    {
        users.signup( registration );
    }

    protected class Users
        implements TransientComposite
    {
        public void signup( Registration registration )
        {
            // Check if user with this name already exists
            events.signedup( registration );
        }
    }
}
