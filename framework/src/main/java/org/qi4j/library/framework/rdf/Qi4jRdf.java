/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.library.framework.rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;


/**
 * This is the RDF vocabulary for Qi4j.
 */
public interface Qi4jRdf
{
    // Namespace TODO: Need to figure out what these should really be!
    String QI4J = "http://www.qi4j.org/rdf/1.0";

    String QI4J_TYPES = "http://www.qi4j.org/rdf/1.0/types#";
    String QI4J_RELATIONSHIPS = "http://www.qi4j.org/rdf/1.0/";
    String QI4J_PROPERTIES = "http://www.qi4j.org/rdf/1.0/properties/";

    // Types
    URI TYPE_APPLICATION = new URIImpl( QI4J_TYPES + "application" );
    URI TYPE_LAYER = new URIImpl( QI4J_TYPES + "layer" );
    URI TYPE_MODULE = new URIImpl( QI4J_TYPES + "module" );
    URI TYPE_ENTITY = new URIImpl( QI4J_TYPES + "entity" );
    URI TYPE_COMPOSITE = new URIImpl( QI4J_TYPES + "composite" );
    URI TYPE_METHOD = new URIImpl( QI4J_TYPES + "method" );
    URI TYPE_CONSTRAINT = new URIImpl( QI4J_TYPES + "constraint" );
    URI TYPE_CONCERN = new URIImpl( QI4J_TYPES + "concern" );
    URI TYPE_CONSTRUCTOR = new URIImpl( QI4J_TYPES + "constructor" );
    URI TYPE_SIDEEFFECT = new URIImpl( QI4J_TYPES + "sideeffect" );
    URI TYPE_MIXIN = new URIImpl( QI4J_TYPES + "mixin" );
    URI TYPE_FIELD = new URIImpl( QI4J_TYPES + "field" );
    URI TYPE_CLASS = new URIImpl( QI4J_TYPES + "class" );
    URI TYPE_OBJECT = new URIImpl( QI4J_TYPES + "object" );
    URI TYPE_PARAMETER = new URIImpl( QI4J_TYPES + "parameter" );
    URI TYPE_INJECTION = new URIImpl( QI4J_TYPES + "injection" );

    // Properties
    URI HAS_INJECTIONS = new URIImpl( QI4J_PROPERTIES + "hasinjections" );

    // Relationship
    URI RELATIONSHIP_PUBLIC_COMPOSITE = new URIImpl( QI4J_RELATIONSHIPS + "publiccomposite" );
    URI RELATIONSHIP_PRIVATE_COMPOSITE = new URIImpl( QI4J_RELATIONSHIPS + "privatecomposite" );
    URI RELATIONSHIP_PRIVATE_METHOD = new URIImpl( QI4J_RELATIONSHIPS + "privatemethod" );
    URI RELATIONSHIP_INJECTION = new URIImpl( QI4J_RELATIONSHIPS + "injection" );
    URI RELATIONSHIP_CONSTRUCTOR = new URIImpl( QI4J_RELATIONSHIPS + "constructor" );
    URI RELATIONSHIP_FIELD = new URIImpl( QI4J_RELATIONSHIPS + "field" );
    URI RELATIONSHIP_APPLIESTO = new URIImpl( QI4J_RELATIONSHIPS + "appliesto" );
    URI RELATION_METHOD = new URIImpl( QI4J_RELATIONSHIPS + "method" );
    URI RELATIONSHIP_CONSTRAINT = new URIImpl( QI4J_RELATIONSHIPS + "constraint" );
    URI RELATIONSHIP_CONCERN = new URIImpl( QI4J_RELATIONSHIPS + "concern" );
    URI RELATIONSHIP_SIDEEFFECT = new URIImpl( QI4J_RELATIONSHIPS + "sideeffect" );
    URI RELATIONSHIP_MIXIN = new URIImpl( QI4J_RELATIONSHIPS + "mixin" );
    URI RELATIONSHIP_LAYER = new URIImpl( QI4J_RELATIONSHIPS + "layer" );
    URI RELATIONSHIP_MODULE = new URIImpl( QI4J_RELATIONSHIPS + "module" );
}
