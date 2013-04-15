/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.sample.rental.web;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.spi.Qi4jSPI;

@Mixins( UrlService.UrlServiceMixin.class )
public interface UrlService
    extends ServiceComposite
{
    void registerBaseUri( String baseUri );

    String createLink( Page page );

    static abstract class UrlServiceMixin
        implements UrlService
    {
        @Structure
        Qi4jSPI spi;

        private String baseUri;

        public void registerBaseUri( String baseUri )
        {
            this.baseUri = baseUri;
        }

        public String createLink( Page page )
        {
            String mountPoint = spi.serviceDescriptorFor( page ).metaInfo( PageMetaInfo.class ).mountPoint();
            return baseUri + mountPoint;
        }
    }
}
