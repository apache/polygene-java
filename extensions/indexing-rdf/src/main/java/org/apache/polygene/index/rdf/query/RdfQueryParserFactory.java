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

package org.apache.polygene.index.rdf.query;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.index.rdf.UnsupportedLanguageException;
import org.apache.polygene.index.rdf.query.internal.RdfQueryParserImpl;
import org.apache.polygene.spi.PolygeneSPI;
import org.apache.polygene.spi.serialization.JsonSerializer;
import org.openrdf.query.QueryLanguage;

@Mixins( RdfQueryParserFactory.RdfQueryParserFactoryMixin.class )
public interface RdfQueryParserFactory
    extends ServiceComposite
{
    RdfQueryParser newQueryParser( QueryLanguage language );

    abstract class RdfQueryParserFactoryMixin
        implements RdfQueryParserFactory
    {
        @Structure
        private PolygeneSPI spi;

        @Service
        private JsonSerializer stateSerializer;

        @Override
        public RdfQueryParser newQueryParser( QueryLanguage language )
        {
            if( language.equals( QueryLanguage.SPARQL ) )
            {
                return new RdfQueryParserImpl( spi, stateSerializer );
            }
            throw new UnsupportedLanguageException( language );
        }
    }
}
