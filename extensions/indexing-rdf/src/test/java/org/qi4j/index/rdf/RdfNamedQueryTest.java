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
package org.qi4j.index.rdf;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.functional.Specification;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.index.rdf.query.SesameExpressions;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.test.indexing.AbstractNamedQueryTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

public class RdfNamedQueryTest extends AbstractNamedQueryTest
{
    @Override
    protected String[] queryStrings()
    {
        return queryStrings;
    }

    @Override
    protected Specification<Composite> createNamedQueryDescriptor( String queryName, String queryString )
    {
        return SesameExpressions.sparql( queryString );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.services( MemoryRepositoryService.class, RdfQueryParserFactory.class ).instantiateOnStartup();
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
        module.services( RdfIndexingEngineService.class );
    }

    private static String[] queryStrings =
    {
        "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "\n" + "}", // script01

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Domain>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 = \"Gaming\")\n" + "}", // script02

        "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "\n" + "}", // script03

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:placeOfBirth ?v0. \n" + "?v0 ns2:name ?v1. \n" + "FILTER (?v1 = \"Kuala Lumpur\")\n" + "}", // script04

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:mother ?v0. \n"
            + "?v0 ns1:placeOfBirth ?v1. \n" + "?v1 ns2:name ?v2. \n" + "FILTER (?v2 = \"Kuala Lumpur\")\n" + "}", // script05

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER (?v0 >= \"1973\")\n" + "}", // script06

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "?entity ns1:placeOfBirth ?v1. \n" + "?v1 ns2:name ?v2. \n"
            + "FILTER ((?v0 >= \"1900\") && (?v2 = \"Penang\"))\n" + "}", // script07

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" + "}", // script08

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Female>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" + "}", // script09

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "FILTER (!(?v0 = \"1975\"))\n" + "}", // script10

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "OPTIONAL {?entity ns1:email ?v0}. \n" + "FILTER (bound(?v0))\n" + "}", // script11

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "OPTIONAL {?entity ns1:email ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script12

        "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (bound(?v0))\n" + "}", // script13

        "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Male>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script14

        "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Male#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "OPTIONAL {?entity ns1:wife ?v0}. \n" + "FILTER (! bound(?v0))\n" + "}", // script15

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "}", // script16

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "} ", // script17

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "\n" + "}\n", // script18

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 > \"D\")\n" + "} ", // script19

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:yearOfBirth ?v0. \n" + "?entity ns2:name ?v1. \n" + "FILTER (?v0 > \"1973\")\n" + "}\n"
            , // script20

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Person#> \n"
            + "PREFIX ns2: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Person>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n"
            + "?entity ns1:placeOfBirth ?v0. \n" + "?v0 ns2:name ?v1. \n" + "?entity ns1:yearOfBirth ?v2. \n" + "\n"
            + "}", // script21

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER regex(?v0,\"J.*Doe\")\n" + "}", // script22

        "", // script23

        "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.indexing.model.Nameable#> \n"
            + "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" + "SELECT DISTINCT ?identity\n"
            + "WHERE {\n" + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.indexing.model.Domain>. \n"
            + "?entity rdf:type ?entityType. \n" + "?entity ns0:identity ?identity. \n" + "?entity ns1:name ?v0. \n"
            + "FILTER (?v0 = ?domain)\n" + "}" // script24

    };
}