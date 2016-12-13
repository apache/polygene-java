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

package org.apache.zest.index.rdf.query;

import org.openrdf.query.QueryLanguage;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.service.qualifier.Tagged;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializer;
import org.apache.zest.index.rdf.UnsupportedLanguageException;
import org.apache.zest.index.rdf.query.internal.RdfQueryParserImpl;
import org.apache.zest.spi.PolygeneSPI;

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
        @Tagged( ValueSerialization.Formats.JSON )
        private ValueSerializer valueSerializer;

        @Override
        public RdfQueryParser newQueryParser( QueryLanguage language )
        {
            if( language.equals( QueryLanguage.SPARQL ) )
            {
                return new RdfQueryParserImpl( spi, valueSerializer );
            }
            throw new UnsupportedLanguageException( language );
        }
    }
}
