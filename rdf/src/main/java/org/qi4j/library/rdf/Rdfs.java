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

public interface Rdfs
{
    String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    URI ID = new URIImpl( RDF + "ID" );

    // Classes
    URI RESOURCE = new URIImpl( RDF + "resource" );
    URI LITERAL = new URIImpl( RDFS + "Literal" );
    URI XML_LITERAL = new URIImpl( RDF + "XMLLiteral" );
    URI CLASS = new URIImpl( RDFS + "Class" );
    URI PROPERTY = new URIImpl( RDF + "Property" );
    URI DATATYPE = new URIImpl( RDFS + "Datatype" );
    URI STATEMENT = new URIImpl( RDF + "Statement" );
    URI BAG = new URIImpl( RDF + "Bag" );
    URI SEQ = new URIImpl( RDF + "Seq" );
    URI ALT = new URIImpl( RDF + "Alt" );
    URI CONTAINER = new URIImpl( RDFS + "Container" );
    URI CONTAINER_MEMBERSHIP_PROPERTY = new URIImpl( RDFS + "ContainerMembershipProperty" );
    URI LIST = new URIImpl( RDF + "List" );
    URI LIST_ITEM = new URIImpl( RDF + "li" );

    // Properties
    URI TYPE = new URIImpl( RDF + "type" );
    URI SUB_CLASS_OF = new URIImpl( RDFS + "subClassOf" );
    URI SUB_PROPERTY_OF = new URIImpl( RDFS + "subPropertyOf" );
    URI DOMAIN = new URIImpl( RDFS + "domain" );
    URI RANGE = new URIImpl( RDFS + "range" );
    URI LABEL = new URIImpl( RDFS + "label" );
    URI COMMENT = new URIImpl( RDFS + "comment" );
    URI MEMBER = new URIImpl( RDFS + "member" );
    URI FIRST = new URIImpl( RDF + "first" );
    URI REST = new URIImpl( RDF + "rest" );
    URI SEE_ALSO = new URIImpl( RDFS + "seeAlso" );
    URI IS_DEFINED_BY = new URIImpl( RDFS + "isDefinedBy" );
    URI VALUE = new URIImpl( RDF + "value" );
    URI SUBJECT = new URIImpl( RDF + "subject" );
    URI PREDICATE = new URIImpl( RDF + "predicate" );
    URI OBJECT = new URIImpl( RDF + "object" );

}
