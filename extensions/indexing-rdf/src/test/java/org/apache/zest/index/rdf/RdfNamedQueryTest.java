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
package org.apache.zest.index.rdf;

import java.util.function.Predicate;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.index.rdf.query.SesameExpressions;
import org.apache.zest.test.indexing.AbstractNamedQueryTest;

public class RdfNamedQueryTest extends AbstractNamedQueryTest
{
    @Override
    protected String[] queryStrings()
    {
        return queryStrings;
    }

    @Override
    protected Predicate<Composite> createNamedQueryDescriptor( String queryName, String queryString )
    {
        return SesameExpressions.sparql( queryString );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new RdfMemoryStoreAssembler( Visibility.module, Visibility.module ).assemble( module );
    }

    private static String[] queryStrings =
    {
// START SNIPPET: query1
        "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "\n" + "}", // script01
// END SNIPPET: query1

// START SNIPPET: query2
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Domain>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 = \"Gaming\")\n" + "}", // script02
// END SNIPPET: query2

// START SNIPPET: query3
        "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "\n" + "}", // script03
// END SNIPPET: query3

// START SNIPPET: query4
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:placeOfBirth ?v0. \n" + "?v0 ns2:name ?v1. \n" + "FILTER (?v1 = \"Kuala Lumpur\")\n" + "}", // script04
// END SNIPPET: query4

// START SNIPPET: query5
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:mother ?v0. \n"
            + "?v0 ns1:placeOfBirth ?v1. \n" + "?v1 ns2:name ?v2. \n" + "FILTER (?v2 = \"Kuala Lumpur\")\n" + "}", // script05
// END SNIPPET: query5

// START SNIPPET: query6
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER (?v0 >= \"1973\")\n" + "}", // script06
// END SNIPPET: query6

// START SNIPPET: query7
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "?entity ns1:placeOfBirth ?v1. \n" + "?v1 ns2:name ?v2. \n"
            + "FILTER ((?v0 >= \"1900\") && (?v2 = \"Penang\"))\n" + "}", // script07
// END SNIPPET: query7

// START SNIPPET: query8
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" + "}", // script08
// END SNIPPET: query8

// START SNIPPET: query9
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Female>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" + "}", // script09
// END SNIPPET: query9

// START SNIPPET: query10
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER (!(?v0 = \"1975\"))\n" + "}", // script10
// END SNIPPET: query10

// START SNIPPET: query11
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "OPTIONAL {?entity ns1:email ?v0}. \n" + "FILTER (bound(?v0))\n" + "}", // script11
// END SNIPPET: query11

// START SNIPPET: query12
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "OPTIONAL {?entity ns1:email ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script12
// END SNIPPET: query12

// START SNIPPET: query13
        "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (bound(?v0))\n" + "}", // script13
// END SNIPPET: query13

// START SNIPPET: query14
        "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Male>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script14
// END SNIPPET: query14

// START SNIPPET: query15
        "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script15
// END SNIPPET: query15

// START SNIPPET: query16
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "}", // script16
// END SNIPPET: query16

// START SNIPPET: query17
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "} ", // script17
// END SNIPPET: query17

// START SNIPPET: query18
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "}\n", // script18
// END SNIPPET: query18

// START SNIPPET: query19
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 > \"D\")\n" + "} ", // script19
// END SNIPPET: query19

// START SNIPPET: query20
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "?entity ns2:name ?v1. \n" + "FILTER (?v0 > \"1973\")\n" + "}\n"
            , // script20
// END SNIPPET: query20

// START SNIPPET: query21
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n"
            + "?entity ns1:placeOfBirth ?v0. \n" + "?v0 ns2:name ?v1. \n" + "?entity ns1:yearOfBirth ?v2. \n" + "\n"
            + "}", // script21
// END SNIPPET: query21

// START SNIPPET: query22
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER regex(?v0,\"J.*Doe\")\n" + "}", // script22
// END SNIPPET: query22

// START SNIPPET: query23
        "", // script23
// END SNIPPET: query23

// START SNIPPET: query24
        "PREFIX ns1: <urn:zest:type:org.apache.zest.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:zest:type:org.apache.zest.api.identity.HasIdentity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?reference\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:zest:type:org.apache.zest.test.indexing.model.Domain>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?reference. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 = ?domain)\n" + "}" // script24
// END SNIPPET: query24
    };
}
