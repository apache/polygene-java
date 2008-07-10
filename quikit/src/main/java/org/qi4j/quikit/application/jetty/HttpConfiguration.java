/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.quikit.application.jetty;

import java.io.Serializable;
import org.qi4j.library.framework.constraint.annotation.Range;

/**
 * Configuration entity for the Jetty Http Server.
 */
public final class HttpConfiguration
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int hostPort;
    private String hostName;
    private String rootContextPath;
    private String resourcePath;

    public HttpConfiguration( int aHostPort )
        throws IllegalArgumentException
    {
        setHostPort( aHostPort );
    }

    public final int getHostPort()
    {
        return hostPort;
    }

    @Range( min = 0, max = 65535 )
    public final void setHostPort( int aHostPort )
        throws IllegalArgumentException
    {
        if( aHostPort < 0 || aHostPort > 65535 )
        {
            throw new IllegalArgumentException( "Port [" + aHostPort + "] is invalid." );
        }
        hostPort = aHostPort;
    }

    public final String getHostName()
    {
        return hostName;
    }

    public final void setHostName( String aHostName )
    {
        hostName = aHostName;
    }

    public final String getRootContextPath()
    {
        return rootContextPath;
    }

    public final void setRootContextPath( String aContextPath )
    {
        rootContextPath = aContextPath;
    }

    public final String getResourcePath()
    {
        return resourcePath;
    }

    public final void setResourcePath( String aResourcePath )
    {
        resourcePath = aResourcePath;
    }
}
