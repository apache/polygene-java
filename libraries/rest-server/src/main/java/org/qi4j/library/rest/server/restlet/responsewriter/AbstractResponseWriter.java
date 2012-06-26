/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.restlet.responsewriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.qi4j.library.rest.server.spi.ResponseWriter;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

/**
 * Helper methods for ResponseWriters
 */
public abstract class AbstractResponseWriter
    implements ResponseWriter
{
    protected static List<Language> ENGLISH = Arrays.asList( Language.ENGLISH );

    protected Variant getVariant( Request request,
                                  List<Language> possibleLanguages,
                                  List<MediaType> possibleMediaTypes
    )
    {
        Language language = request.getClientInfo().getPreferredLanguage( possibleLanguages );

        if( language == null )
        {
            language = possibleLanguages.get( 0 );
        }

        MediaType responseType = request.getClientInfo().getPreferredMediaType( possibleMediaTypes );

        if( responseType == null && request.getClientInfo()
                                        .getPreferredMediaType( Collections.singletonList( MediaType.ALL ) ) == MediaType.ALL )
        {
            responseType = possibleMediaTypes.get( 0 );
        }

        Variant variant = new Variant( responseType, language );
        variant.setCharacterSet( CharacterSet.UTF_8 );

        return variant;
    }
}
