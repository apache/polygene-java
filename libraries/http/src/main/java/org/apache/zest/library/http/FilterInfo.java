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
package org.apache.zest.library.http;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Map;
import javax.servlet.DispatcherType;

public class FilterInfo
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String path;
    private final Map<String, String> initParameters;
    private final EnumSet<DispatcherType> dispatchers;

    public FilterInfo( String path, Map<String, String> initParameters, EnumSet<DispatcherType> dispatchers )
    {
        this.dispatchers = dispatchers;
        this.initParameters = initParameters;
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }

    public Map<String, String> initParameters()
    {
        return initParameters;
    }

    public EnumSet<DispatcherType> dispatchers()
    {
        return dispatchers;
    }
}
