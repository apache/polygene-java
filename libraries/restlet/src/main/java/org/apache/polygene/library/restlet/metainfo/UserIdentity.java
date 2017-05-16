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

package org.apache.polygene.library.restlet.metainfo;

public class UserIdentity
{
    private final String identifier;
    private final String name;
    private final String email;
    private final String firstName;
    private final String lastName;

    public UserIdentity( String identifier, String name, String email, String firstName, String lastName )
    {
        this.identifier = identifier;
        this.name = name;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String identifier()
    {
        return identifier;
    }

    public String name()
    {
        return name;
    }

    public String email()
    {
        return email;
    }

    public String firstName()
    {
        return firstName;
    }

    public String lastName()
    {
        return lastName;
    }


}
