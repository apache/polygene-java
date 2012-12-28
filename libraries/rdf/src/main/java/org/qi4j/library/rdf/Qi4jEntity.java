/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;


/**
 * This is the RDF vocabulary for Qi4j Entity data.
 */
public interface Qi4jEntity
{
    // Model
    String NAMESPACE = "http://www.qi4j.org/rdf/entity/1.0/";

    // Types
    URI ENTITY = new URIImpl( NAMESPACE + "entity" );
    URI ENTITYTYPEREFERENCE = new URIImpl( NAMESPACE + "entitytypereference" );
    URI QUALIFIER = new URIImpl( NAMESPACE + "qualifier" );
}