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

package org.apache.polygene.library.rdf;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;


/**
 * This is the RDF vocabulary for Polygene EntityType data.
 */
public interface PolygeneEntityType
{
    // Namespace
    String NAMESPACE = "http://polygene.apache.org/rdf/entitytype/1.0/";

    // Predicates
    URI QUERYABLE = new URIImpl( NAMESPACE + "queryable" );
    URI VERSION = new URIImpl( NAMESPACE + "version" );
    URI TYPE = new URIImpl( NAMESPACE + "type" );
}