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
package org.qi4j.index.rdf.query;

import org.openrdf.query.QueryLanguage;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializer;
import org.qi4j.index.rdf.UnsupportedLanguageException;
import org.qi4j.index.rdf.query.internal.RdfQueryParserImpl2;
import org.qi4j.spi.Qi4jSPI;

@Mixins( RdfQueryParserFactory.RdfQueryParserFactoryMixin.class )
public interface RdfQueryParserFactory
    extends ServiceComposite
{
    RdfQueryParser newQueryParser( QueryLanguage language );

    abstract class RdfQueryParserFactoryMixin
        implements RdfQueryParserFactory
    {
        @Structure
        private Qi4jSPI spi;
        @Service
        @Tagged( ValueSerialization.Formats.JSON )
        private ValueSerializer valueSerializer;

        @Override
        public RdfQueryParser newQueryParser( QueryLanguage language )
        {
            if( language.equals( QueryLanguage.SPARQL ) )
            {
                return new RdfQueryParserImpl2( spi, valueSerializer );
            }
            throw new UnsupportedLanguageException( language );
        }
    }
}
