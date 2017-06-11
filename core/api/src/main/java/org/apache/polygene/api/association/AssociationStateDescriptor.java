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
package org.apache.polygene.api.association;

import java.util.stream.Stream;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.composite.StateDescriptor;

/**
 * Associations State Descriptor.
 */
public interface AssociationStateDescriptor extends StateDescriptor
{
    AssociationDescriptor getAssociationByName( String name )
        throws IllegalArgumentException;

    AssociationDescriptor getAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException;

    boolean hasAssociation( QualifiedName name );

    AssociationDescriptor getManyAssociationByName( String name )
        throws IllegalArgumentException;

    AssociationDescriptor getManyAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException;

    boolean hasManyAssociation( QualifiedName name );

    AssociationDescriptor getNamedAssociationByName( String name )
        throws IllegalArgumentException;

    AssociationDescriptor getNamedAssociationByQualifiedName( QualifiedName name )
        throws IllegalArgumentException;

    boolean hasNamedAssociation( QualifiedName name );

    Stream<? extends AssociationDescriptor> associations();

    Stream<? extends AssociationDescriptor> manyAssociations();

    Stream<? extends AssociationDescriptor> namedAssociations();
}
