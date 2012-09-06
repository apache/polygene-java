/*
 * Copyright (c) 2012, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.api.configuration;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.constraints.annotation.Email;
import org.qi4j.library.constraints.annotation.MinLength;

// Documentation Support
@Mixins( MailService.MailServiceMixin.class )
public interface MailService
{
    void sendMail( @Email String to, @MinLength( 8 ) String subject, String body );
    
    // START SNIPPET: write
    void changeExternalMailService( String hostName, int port );
    // END SNIPPET: write
    
    public class MailServiceMixin
        implements MailService
    {
        // START SNIPPET: read        
        @This
        private Configuration<MailServiceConfiguration> config;

        @Override
        public void sendMail( @Email String to, @MinLength( 8 ) String subject, String body )
        {
            config.refresh();
            MailServiceConfiguration conf = config.get();
            String hostName = conf.hostName().get();
            int port = conf.port().get();
            // END SNIPPET: read

            // START SNIPPET: read        
        }
        // END SNIPPET: read        

        // START SNIPPET: write        
        @Override
        public void changeExternalMailService( String hostName, int port )
        {
            MailServiceConfiguration conf = config.get();
            conf.hostName().set( hostName );
            conf.port().set( port );
            config.save();
        }
        // START SNIPPET: write        
    }
}
