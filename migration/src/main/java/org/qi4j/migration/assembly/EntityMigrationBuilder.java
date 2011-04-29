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

package org.qi4j.migration.assembly;

import org.qi4j.migration.operation.AddAssociation;
import org.qi4j.migration.operation.AddManyAssociation;
import org.qi4j.migration.operation.AddProperty;
import org.qi4j.migration.operation.RemoveAssociation;
import org.qi4j.migration.operation.RemoveManyAssociation;
import org.qi4j.migration.operation.RemoveProperty;
import org.qi4j.migration.operation.RenameAssociation;
import org.qi4j.migration.operation.RenameManyAssociation;
import org.qi4j.migration.operation.RenameProperty;

/**
 * Fluent API for creating migration rules for specific entity types.
 */
public class EntityMigrationBuilder
{
    private VersionMigrationBuilder migrationBuilder;
    private String[] entityTypes;

    public EntityMigrationBuilder( VersionMigrationBuilder migrationBuilder, String[] entityTypes )
    {
        this.migrationBuilder = migrationBuilder;
        this.entityTypes = entityTypes;
    }

    /**
     * Return the version builder
     *
     * @return current version builder
     */
    public VersionMigrationBuilder end()
    {
        return migrationBuilder;
    }

    // Operations on entities

    /**
     * Add rule to rename an Entity property.
     *
     * @param from property name
     * @param to   property name
     *
     * @return the builder
     */
    public EntityMigrationBuilder renameProperty( String from, String to )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RenameProperty( from, to ) ) );

        return this;
    }

    /**
     * Add rule to add an Entity property.
     *
     * @param property     to be added
     * @param defaultValue default value
     *
     * @return the builder
     */
    public EntityMigrationBuilder addProperty( String property, Object defaultValue )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new AddProperty( property, defaultValue ) ) );

        return this;
    }

    /**
     * Add rule to remove an Entity property
     *
     * @param property     to be removed
     * @param defaultValue default value (used for downgrading)
     *
     * @return the builder
     */
    public EntityMigrationBuilder removeProperty( String property, String defaultValue )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RemoveProperty( property, defaultValue ) ) );

        return this;
    }

    /**
     * Add rule to rename an Entity association.
     *
     * @param from assocation name
     * @param to   association name
     *
     * @return the builder
     */
    public EntityMigrationBuilder renameAssociation( String from, String to )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RenameAssociation( from, to ) ) );

        return this;
    }

    /**
     * Add rule to add an Entity association.
     *
     * @param association      to be added
     * @param defaultReference default reference
     *
     * @return the builder
     */
    public EntityMigrationBuilder addAssociation( String association, String defaultReference )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new AddAssociation( association, defaultReference ) ) );

        return this;
    }

    /**
     * Add rule to remove an Entity association
     *
     * @param association      to be removed
     * @param defaultReference default value (used for downgrading)
     *
     * @return the builder
     */
    public EntityMigrationBuilder removeAssociation( String association, String defaultReference )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RemoveAssociation( association, defaultReference ) ) );

        return this;
    }

    /**
     * Add rule to add an Entity many-association.
     *
     * @param association       to be added
     * @param defaultReferences default reference
     *
     * @return the builder
     */
    public EntityMigrationBuilder addManyAssociation( String association, String... defaultReferences )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new AddManyAssociation( association, defaultReferences ) ) );

        return this;
    }

    /**
     * Add rule to remove an Entity association
     *
     * @param association       to be removed
     * @param defaultReferences default value (used for downgrading)
     *
     * @return the builder
     */
    public EntityMigrationBuilder removeManyAssociation( String association, String... defaultReferences )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RemoveManyAssociation( association, defaultReferences ) ) );

        return this;
    }

    /**
     * Add rule to rename an Entity many-association.
     *
     * @param from many-assocation name
     * @param to   many-association name
     *
     * @return the builder
     */
    public EntityMigrationBuilder renameManyAssociation( String from, String to )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    new RenameManyAssociation( from, to ) ) );

        return this;
    }

    /**
     * Add rule to perform a custom operation
     *
     * @param operationEntity the custom operation to be performed during migration
     *
     * @return the builder
     */
    public EntityMigrationBuilder custom( EntityMigrationOperation operationEntity )
    {
        migrationBuilder.builder.getEntityRules().addRule( new EntityMigrationRule( migrationBuilder.fromVersion,
                                                                                    migrationBuilder.toVersion,
                                                                                    entityTypes,
                                                                                    operationEntity ) );

        return this;
    }
}
