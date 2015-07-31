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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.library.constraints.annotation.GreaterThan;
import org.qi4j.library.constraints.annotation.Range;

/**
 * Configuration of the JettyService.
 * 
 * The only mandatory property is port.
 * Other properties are provided with a sensible default respecting the original Jetty defaults.
 */
// START SNIPPET: config
public interface JettyConfiguration
        extends ConfigurationComposite
{

    // ## Connector Configuration Properties ##################################
    /**
     * @return Port on which the {@link JettyService} will listen for incomming requests.
     */
    @Range( min = 0, max = 65535 )
    Property<Integer> port();

    /**
     * Name or ip representing the interface on which the {@link JettyService} will bind the server socket.
     * 
     * If not set the {@link JettyService} will be bound to all interfaces.
     * 
     * @return Name or ip representing the interface on which the {@link JettyService} will bind the server socket.
     */
    @Optional
    Property<String> hostName();

    /**
     * Defaults to false.
     * 
     * @return If the {@link JettyService} should collect statistics.
     */
    @UseDefaults
    Property<Boolean> statistics();

    /**
     * @return Maximum Idle time for connections in milliseconds.
     */
    @Optional
    Property<Integer> maxIdleTime();

    /**
     * @return Maximum Idle time for connections in milliseconds when resources are low.
     */
    @Optional
    Property<Integer> lowResourceMaxIdleTime();

    /**
     * @return The size of the buffer to be used for request headers in bytes.
     */
    @Optional
    @GreaterThan( 0 )
    Property<Integer> requestHeaderSize();

    /**
     * @return The size of the buffer to be used for response headers in bytes.
     */
    @Optional
    @GreaterThan( 0 )
    Property<Integer> responseHeaderSize();

    /**
     * Size of the content buffer for sending responses in bytes.
     * 
     * These buffers are only used for active connections that are sending responses with bodies that will not fit
     * within the header buffer.
     * 
     * @return The size of the content buffer for sending responses in bytes.
     */
    @Optional
    @GreaterThan( 0 )
    Property<Integer> responseBufferSize();

    // ## ServletContext Configuration Properties #############################
    /**
     * url prefix of the {@link JettyService}.
     * 
     * Defaulted to "/".
     * 
     * @return The url prefix of the {@link JettyService}.
     */
    @Optional
    Property<String> contextPath();

    @Optional
    Property<String> resourcePath();

    /**
     * Virtual hosts for this JettyService.
     * 
     * Coma separated list of hostnames.
     * 
     * @return Virtual hosts for this JettyService.
     */
    @Optional
    Property<String> virtualHosts();

    /**
     * Welcome files names for this JettyService.
     * 
     * Coma separated list of welcome files.
     * Defaulted to index.html.
     * 
     * @return Welcome files names for this JettyService.
     */
    @Optional
    Property<String> welcomeFiles();

    /**
     * @return Maximum Form content size.
     */
    @Optional
    @GreaterThan( 0 )
    Property<Integer> maxFormContentSize();

    // ## Server Configuration Properties #####################################
    /**
     * @return If {@link JettyService} populates http header with server date.
     */
    @Optional
    Property<Boolean> sendDateHeader();

    /**
     * @return If {@link JettyService} expose it's implementation version.
     */
    @Optional
    Property<Boolean> sendServerVersion();

    /**
     * Timeout use to wait for active requests to terminate on shutdown in milliseconds.
     * 
     * Jetty will stop gracefully only if this property is set.
     * 
     * @return Timeout use to wait for active requests to terminate on shutdown in milliseconds.
     */
    @Optional
    @GreaterThan( 0 )
    Property<Integer> gracefullShutdownTimeout();

}
// END SNIPPET: config
