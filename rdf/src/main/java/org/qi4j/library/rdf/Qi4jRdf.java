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
 * This is the RDF vocabulary for Qi4j.
 */
public interface Qi4jRdf
{

    // MODEL
    // Namespace TODO: Need to figure out what these should really be!
    String QI4JMODEL = "http://www.qi4j.org/rdf/model/1.0/";

    String QI4JMODEL_TYPES = "http://www.qi4j.org/rdf/model/1.0/type#";
    String QI4JMODEL_RELATIONSHIPS = "http://www.qi4j.org/rdf/module/1.0/";
    String QI4JMODEL_PROPERTIES = "http://www.qi4j.org/rdf/model/1.0/property#";

    // Types
    URI TYPE_APPLICATION = new URIImpl( QI4JMODEL_TYPES + "application" );
    URI TYPE_LAYER = new URIImpl( QI4JMODEL_TYPES + "layer" );
    URI TYPE_MODULE = new URIImpl( QI4JMODEL_TYPES + "module" );
    URI TYPE_ENTITY = new URIImpl( QI4JMODEL_TYPES + "entity" );
    URI TYPE_QUALIFIER = new URIImpl( QI4JMODEL_TYPES + "qualifier" );
    URI TYPE_COMPOSITE = new URIImpl( QI4JMODEL_TYPES + "composite" );
    URI TYPE_SERVICE = new URIImpl( QI4JMODEL_TYPES + "service" );
    URI TYPE_METHOD = new URIImpl( QI4JMODEL_TYPES + "method" );
    URI TYPE_CONSTRAINT = new URIImpl( QI4JMODEL_TYPES + "constraint" );
    URI TYPE_CONCERN = new URIImpl( QI4JMODEL_TYPES + "concern" );
    URI TYPE_CONSTRUCTOR = new URIImpl( QI4JMODEL_TYPES + "constructor" );
    URI TYPE_SIDEEFFECT = new URIImpl( QI4JMODEL_TYPES + "sideeffect" );
    URI TYPE_MIXIN = new URIImpl( QI4JMODEL_TYPES + "mixin" );
    URI TYPE_FIELD = new URIImpl( QI4JMODEL_TYPES + "field" );
    URI TYPE_CLASS = new URIImpl( QI4JMODEL_TYPES + "class" );
    URI TYPE_OBJECT = new URIImpl( QI4JMODEL_TYPES + "object" );
    URI TYPE_PARAMETER = new URIImpl( QI4JMODEL_TYPES + "parameter" );
    URI TYPE_INJECTION = new URIImpl( QI4JMODEL_TYPES + "injection" );
    URI TYPE_INFO = new URIImpl( QI4JMODEL_TYPES + "info" );

    // Properties
    URI HAS_INJECTIONS = new URIImpl( QI4JMODEL_PROPERTIES + "hasinjections" );

    // Relationship
    URI RELATIONSHIP_COMPOSITE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "composite" );
    URI RELATIONSHIP_ENTITY = new URIImpl( QI4JMODEL_RELATIONSHIPS + "entity" );
    URI RELATIONSHIP_SERVICE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "service" );
    URI RELATIONSHIP_OBJECT = new URIImpl( QI4JMODEL_RELATIONSHIPS + "object" );
    URI RELATIONSHIP_PRIVATE_METHOD = new URIImpl( QI4JMODEL_RELATIONSHIPS + "private/method" );
    URI RELATIONSHIP_INJECTION = new URIImpl( QI4JMODEL_RELATIONSHIPS + "injection" );
    URI RELATIONSHIP_CONSTRUCTOR = new URIImpl( QI4JMODEL_RELATIONSHIPS + "constructor" );
    URI RELATIONSHIP_FIELD = new URIImpl( QI4JMODEL_RELATIONSHIPS + "field" );
    URI RELATIONSHIP_APPLIESTO = new URIImpl( QI4JMODEL_RELATIONSHIPS + "appliesto" );
    URI RELATIONSHIP_METHOD = new URIImpl( QI4JMODEL_RELATIONSHIPS + "method" );
    URI RELATIONSHIP_CONSTRAINT = new URIImpl( QI4JMODEL_RELATIONSHIPS + "constraint" );
    URI RELATIONSHIP_CONCERN = new URIImpl( QI4JMODEL_RELATIONSHIPS + "concern" );
    URI RELATIONSHIP_SIDEEFFECT = new URIImpl( QI4JMODEL_RELATIONSHIPS + "sideeffect" );
    URI RELATIONSHIP_PUBLIC_SERVICE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "public/service" );
    URI RELATIONSHIP_PRIVATE_SERVICE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "private/service" );
    URI RELATIONSHIP_PROVIDEDBY = new URIImpl( QI4JMODEL_RELATIONSHIPS + "providedby" );
    URI RELATIONSHIP_SERVICEINFO = new URIImpl( QI4JMODEL_RELATIONSHIPS + "info/service" );
    URI RELATIONSHIP_INFOVALUE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "info/value" );
    URI RELATIONSHIP_MIXIN = new URIImpl( QI4JMODEL_RELATIONSHIPS + "mixin" );
    URI RELATIONSHIP_LAYER = new URIImpl( QI4JMODEL_RELATIONSHIPS + "layer" );
    URI RELATIONSHIP_MODULE = new URIImpl( QI4JMODEL_RELATIONSHIPS + "module" );
}
