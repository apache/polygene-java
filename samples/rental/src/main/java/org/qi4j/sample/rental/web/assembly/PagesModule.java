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

package org.qi4j.sample.rental.web.assembly;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.sample.rental.web.BookingPage;
import org.qi4j.sample.rental.web.MainPage;
import org.qi4j.sample.rental.web.PageMetaInfo;
import org.qi4j.sample.rental.web.UrlService;

public class PagesModule
    implements Assembler
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        PageMetaInfo mainpageMeta = new PageMetaInfo( "main" );
        module.services( MainPage.class ).setMetaInfo( mainpageMeta );

        PageMetaInfo bookingpageMeta = new PageMetaInfo( "booking" );
        module.services( BookingPage.class ).setMetaInfo( bookingpageMeta );

        module.services( UrlService.class );
    }
}
