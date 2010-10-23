/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.migration;

import org.qi4j.api.common.QualifiedName;

/**
 * Implement this as a service to receive events from the Migration process.
 */
public interface MigrationEvents
{
    void propertyAdded( String entity, QualifiedName name, String value );

    void propertyRemoved( String entity, QualifiedName name );

    void propertyRenamed( String entity, QualifiedName from, QualifiedName to );

    void associationAdded( String entity, QualifiedName name, String defaultReference );

    void associationRemoved( String entity, QualifiedName name );

    void associationRenamed( String entity, QualifiedName from, QualifiedName to );

    void manyAssociationAdded( String entity, QualifiedName name, String... defaultReferences );

    void manyAssociationRemoved( String entity, QualifiedName name );

    void manyAssociationRenamed( String entity, QualifiedName from, QualifiedName to );

    void entityTypeChanged( String entity, String newEntityType );
}
