/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.http;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * JAVADOC
 */
public class ServletInfo
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String path;
    private final Map<String, String> initParams;

    public ServletInfo( String mountPath )
    {
        this( mountPath, Collections.<String, String>emptyMap() );
    }

    public ServletInfo( String mountPath, Map<String, String> initParams )
    {
        path = mountPath;
        this.initParams = initParams;
    }

    public String getPath()
    {
        return path;
    }

    public Map<String, String> initParams()
    {
        return initParams;
    }
}