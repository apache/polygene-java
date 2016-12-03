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

package org.apache.zest.api.configuration;

import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.identity.Identity;

public class NoSuchConfigurationException extends RuntimeException
{
    private final Class<? extends HasIdentity> configType;
    private final Identity identity;

    public NoSuchConfigurationException( Class<? extends HasIdentity> configType,
                                         Identity identity,
                                         Exception cause
    )
    {
        super( "No configuration found for '" + identity + "' and configuration " + configType.getName() + " has one or more non-Optional properties.", cause );
        this.configType = configType;
        this.identity = identity;
    }

    public Class<? extends HasIdentity> configType()
    {
        return configType;
    }

    public Identity identity()
    {
        return identity;
    }
}
