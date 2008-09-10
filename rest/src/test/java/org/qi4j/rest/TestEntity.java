/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.rest;

import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.RDF;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.entity.association.Qualifier;
import org.qi4j.library.rdf.DcRdf;
import org.qi4j.property.Property;

/**
 * TODO
 */
public interface TestEntity
    extends EntityComposite
{
    @RDF( DcRdf.DC + "title" ) Property<String> name();

    Property<Integer> age();

    Property<String> unsetName();

    Association<TestEntity> association();

    Association<TestEntity> unsetAssociation();

    ManyAssociation<TestEntity> manyAssociation();

    @RDF( "http://www.w3.org/2001/vcard-rdf/3.0#GROUP" ) ListAssociation<TestEntity> listAssociation();

    SetAssociation<TestEntity> setAssociation();

    ManyAssociation<Qualifier<TestEntity,TestRole>> manyAssociationQualifier();
}
