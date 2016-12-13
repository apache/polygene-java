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

package org.apache.zest.library.rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;


/**
 * This is the RDF vocabulary for Polygene.
 */
public interface PolygeneRdf
{

    // MODEL
    // Namespace TODO: Need to figure out what these should really be!
    String ZEST_MODEL = "http://zest.apache.org/rdf/model/1.0/";

    String ZEST_MODEL_TYPES = "http://zest.apache.org/rdf/model/1.0/type#";
    String ZEST_MODEL_RELATIONSHIPS = "http://zest.apache.org/rdf/module/1.0/";
    String ZEST_MODEL_PROPERTIES = "http://zest.apache.org/rdf/model/1.0/property#";

    // Types
    URI TYPE_APPLICATION = new URIImpl( ZEST_MODEL_TYPES + "application" );
    URI TYPE_LAYER = new URIImpl( ZEST_MODEL_TYPES + "layer" );
    URI TYPE_MODULE = new URIImpl( ZEST_MODEL_TYPES + "module" );
    URI TYPE_ENTITY = new URIImpl( ZEST_MODEL_TYPES + "entity" );
    URI TYPE_QUALIFIER = new URIImpl( ZEST_MODEL_TYPES + "qualifier" );
    URI TYPE_COMPOSITE = new URIImpl( ZEST_MODEL_TYPES + "composite" );
    URI TYPE_SERVICE = new URIImpl( ZEST_MODEL_TYPES + "service" );
    URI TYPE_METHOD = new URIImpl( ZEST_MODEL_TYPES + "method" );
    URI TYPE_CONSTRAINT = new URIImpl( ZEST_MODEL_TYPES + "constraint" );
    URI TYPE_CONCERN = new URIImpl( ZEST_MODEL_TYPES + "concern" );
    URI TYPE_CONSTRUCTOR = new URIImpl( ZEST_MODEL_TYPES + "constructor" );
    URI TYPE_SIDEEFFECT = new URIImpl( ZEST_MODEL_TYPES + "sideeffect" );
    URI TYPE_MIXIN = new URIImpl( ZEST_MODEL_TYPES + "mixin" );
    URI TYPE_FIELD = new URIImpl( ZEST_MODEL_TYPES + "field" );
    URI TYPE_CLASS = new URIImpl( ZEST_MODEL_TYPES + "class" );
    URI TYPE_OBJECT = new URIImpl( ZEST_MODEL_TYPES + "object" );
    URI TYPE_PARAMETER = new URIImpl( ZEST_MODEL_TYPES + "parameter" );
    URI TYPE_INJECTION = new URIImpl( ZEST_MODEL_TYPES + "injection" );
    URI TYPE_INFO = new URIImpl( ZEST_MODEL_TYPES + "info" );

    // Properties
    URI HAS_INJECTIONS = new URIImpl( ZEST_MODEL_PROPERTIES + "hasinjections" );

    // Relationship
    URI RELATIONSHIP_COMPOSITE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "composite" );
    URI RELATIONSHIP_ENTITY = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "entity" );
    URI RELATIONSHIP_SERVICE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "service" );
    URI RELATIONSHIP_OBJECT = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "object" );
    URI RELATIONSHIP_PRIVATE_METHOD = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "private/method" );
    URI RELATIONSHIP_INJECTION = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "injection" );
    URI RELATIONSHIP_CONSTRUCTOR = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "constructor" );
    URI RELATIONSHIP_FIELD = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "field" );
    URI RELATIONSHIP_APPLIESTO = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "appliesto" );
    URI RELATIONSHIP_METHOD = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "method" );
    URI RELATIONSHIP_CONSTRAINT = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "constraint" );
    URI RELATIONSHIP_CONCERN = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "concern" );
    URI RELATIONSHIP_SIDEEFFECT = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "sideeffect" );
    URI RELATIONSHIP_PUBLIC_SERVICE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "public/service" );
    URI RELATIONSHIP_PRIVATE_SERVICE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "private/service" );
    URI RELATIONSHIP_PROVIDEDBY = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "providedby" );
    URI RELATIONSHIP_SERVICEINFO = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "info/service" );
    URI RELATIONSHIP_INFOVALUE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "info/value" );
    URI RELATIONSHIP_MIXIN = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "mixin" );
    URI RELATIONSHIP_LAYER = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "layer" );
    URI RELATIONSHIP_MODULE = new URIImpl( ZEST_MODEL_RELATIONSHIPS + "module" );
}
