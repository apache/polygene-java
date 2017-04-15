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
package org.apache.polygene.library.sql.generator.grammar.factories;

import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.ForeignKeyConstraintBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.SchemaDefinitionBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.TableDefinitionBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.TableElementListBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.UniqueConstraintBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.ViewDefinitionBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.AutoGenerationPolicy;
import org.apache.polygene.library.sql.generator.grammar.definition.table.CheckConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ConstraintCharacteristics;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ForeignKeyConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.LikeClause;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.view.RegularViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewDefinition;

/**
 * This is factory for creating builders and syntax elements related to SQL Data Definition (typically {@code CREATE}
 * statements).
 *
 * @author Stanislav Muhametsin
 */
public interface DefinitionFactory
{

    /**
     * Creates an empty builder for {@code CREATE SCHEMA} statement.
     *
     * @return An empty builder for {@code CREATE SCHEMA} statement.
     */
    SchemaDefinitionBuilder createSchemaDefinitionBuilder();

    /**
     * Creates an empty builder for {@code CREATE TABLE} statement.
     *
     * @return An empty builder for {@code CREATE TABLE} statement.
     */
    TableDefinitionBuilder createTableDefinitionBuilder();

    /**
     * Creates an empty builder for defining columns and constraints for {@code CREATE TABLE} statement.
     *
     * @return An empty builder for defining columns and constraints for {@code CREATE TABLE} statement.
     * @see TableDefinition
     */
    TableElementListBuilder createTableElementListBuilder();

    /**
     * Creates a new definition of column with specified name and data type. Invoking this method is equivalent to
     * invoking {@link #createColumnDefinition(String, String, String, Boolean)} and pass {@code null} and {@code true}
     * as last two parameters (meaning that there is no default value for column, and it may have {@code NULL} values).
     *
     * @param columnName     The name of the column.
     * @param columnDataType The data type of the column.
     * @return The syntax element for definition of column with specified name and data type.
     * @see #createColumnDefinition(String, String, String, Boolean)
     */
    ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType );

    /**
     * Creates a new definition of column with specified name, data type, and {@code NULL} value policy. Invoking this
     * method is equivalent to invoking {@link #createColumnDefinition(String, String, String, Boolean)} and pass
     * {@code null} and {@code mayBeNull} as last two parameters (meaning that there is no default value for column).
     *
     * @param columnName     The name of the column.
     * @param columnDataType The data type of the column.
     * @param mayBeNull      The {@code NULL} value policy. Setting this to {@code false} is same as specifying
     *                       {@code NOT NULL} in column definition in SQL.
     * @return The syntax element for definition of column with specified name, data type, and {@code NULL} value
     * policy.
     * @see #createColumnDefinition(String, String, String, Boolean)
     */
    ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, Boolean mayBeNull );

    /**
     * Creates a new definition of column with specified name, data type, default value. Invoking this method is
     * equivalent to invoking {@link #createColumnDefinition(String, String, String, Boolean)} and pass
     * {@code columnDefault} and {@code true} as last two parameters (meaning that column may have {@code NULL} values).
     *
     * @param columnName     The name of the column.
     * @param columnDataType The data type of the column.
     * @param columnDefault  The default value of the column.
     * @return The syntax element for definition of column with specified name, data type, default value.
     * @see #createColumnDefinition(String, String, String, Boolean)
     */
    ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, String columnDefault );

    /**
     * Creates a new definition of column with specified name, data type, default value, and {@code NULL} value policy.
     * Invoking this method is equivalent to invoking
     * {@link #createColumnDefinition(String, SQLDataType, String, Boolean, AutoGenerationPolicy)} and pass {@code null}
     * as last parameter.
     *
     * @param columnName     The name of the column.
     * @param columnDataType The data type of the column.
     * @param columnDefault  The default value of the column.
     * @param mayBeNull      The {@code NULL} value policy for the column. Setting this to {@code false} is same as
     *                       specifying {@code NOT NULL} in column definition in SQL.
     * @return The syntax element for definition of column with specified name, data type, default value, and
     * {@code NULL} value policy.
     */
    ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType,
                                             String columnDefault, Boolean mayBeNull );

    /**
     * Creates a new definition of column with specified name, data type, {@code NULL} value policy, and auto generation
     * policy.
     *
     * @param columnName           The name of the column.
     * @param columnDataType       The data type of the column.
     * @param mayBeNull            The {@code NULL} value policy for the column. Setting this to {@code false} is same as
     *                             specifying {@code NOT NULL} in column definition in SQL.
     * @param autoGenerationPolicy The policy for auto generation for this column. Should be {@code null} if the column
     *                             should not be marked as automatically generated.
     * @return The syntax element for definition of column with specified name, data type, default value, {@code NULL}
     * value policy, and auto generation policy.
     */

    ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, Boolean mayBeNull,
                                             AutoGenerationPolicy autoGenerationPolicy );

    /**
     * Creates a new {@code LIKE
     * <table name>} clause for {@code CREATE TABLE} statement.
     *
     * @param tableName The name of the target table.
     * @return The syntax element for {@code LIKE
     * <table name>} clause for {@code CREATE TABLE} statement.
     */
    LikeClause createLikeClause( TableNameDirect tableName );

    /**
     * Creates a new unnamed table constraint without any {@link ConstraintCharacteristics}. Invoking this method is
     * equivalent to invoking
     * {@link #createTableConstraintDefinition(String, TableConstraint, ConstraintCharacteristics)} and passing
     * {@code null}s as first and last parameters.
     *
     * @param constraint The constraint for the table.
     * @return The syntax element for unnamed table constraint without any {@link ConstraintCharacteristics}.
     * @see #createColumnDefinition(String, String, String, Boolean)
     */
    TableConstraintDefinition createTableConstraintDefinition( TableConstraint constraint );

    /**
     * Creates a new, named table constraint without any {@link ConstraintCharacteristics}. Invoking this method is
     * equivalent to invoking
     * {@link #createTableConstraintDefinition(String, TableConstraint, ConstraintCharacteristics)} and passing
     * {@code null} as last parameter.
     *
     * @param name       The name for the constraint.
     * @param constraint The constraint for the table.
     * @return The syntax element for named table constraint without any {@link ConstraintCharacteristics}.
     * @see #createColumnDefinition(String, String, String, Boolean)
     */
    TableConstraintDefinition createTableConstraintDefinition( String name, TableConstraint constraint );

    /**
     * Creates a new unnamed table constraint with specified {@link ConstraintCharacteristics}. Invoking this method is
     * equivalent to invoking
     * {@link #createTableConstraintDefinition(String, TableConstraint, ConstraintCharacteristics)} and passing
     * {@code null} as first parameter.
     *
     * @param constraint      The constraint for the table.
     * @param characteristics The constraint characteristics for the constraint.
     * @return The syntax element for unnamed table constraint with specified {@link ConstraintCharacteristics}.
     * @see #createColumnDefinition(String, String, String, Boolean)
     * @see ConstraintCharacteristics
     */
    TableConstraintDefinition createTableConstraintDefinition( TableConstraint constraint,
                                                               ConstraintCharacteristics characteristics );

    /**
     * Creates a new named table constraint with specified {@link ConstraintCharacteristics}.
     *
     * @param name            The name for the constraint.
     * @param constraint      The constraint for the table.
     * @param characteristics The characteristics for the constraint.
     * @return The syntax element for named table constraint with specified {@link ConstraintCharacteristics}.
     * @see ConstraintCharacteristics
     */
    TableConstraintDefinition createTableConstraintDefinition( String name, TableConstraint constraint,
                                                               ConstraintCharacteristics characteristics );

    /**
     * Creates a {@code CHECK} clause, typically used in {@code CREATE TABLE} statements.
     *
     * @param check The boolean expression for check.
     * @return The syntax element for {@code CHECK} clause, typically used in {@code CREATE TABLE} statements.
     */
    CheckConstraint createCheckConstraint( BooleanExpression check );

    /**
     * Creates an empty builder for unique constraint (either {@code UNIQUE(columns...)} or
     * {@code PRIMARY KEY(columns...)}), typically used in {@code CREATE TABLE} statements.
     *
     * @return An empty builder for unique constraints (either {@code UNIQUE(columns...)} or
     * {@code PRIMARY KEY(columns...)}).
     */
    UniqueConstraintBuilder createUniqueConstraintBuilder();

    /**
     * Creates an empty builder for {@code FOREIGN KEY} constraint, typically used in {@code CREATE TABLE} statements.
     *
     * @return An empty builder for {@code FOREIGN KEY} constraint.
     * @see ForeignKeyConstraintBuilder
     * @see ForeignKeyConstraint
     */
    ForeignKeyConstraintBuilder createForeignKeyConstraintBuilder();

    /**
     * Creates an empty builder for {@code CREATE VIEW} statement.
     *
     * @return An empty builder for {@code CREATE VIEW} statement.
     * @see ViewDefinitionBuilder
     * @see ViewDefinition
     */
    ViewDefinitionBuilder createViewDefinitionBuilder();

    /**
     * Creates a new view specification with given columns. Must have at least one column.
     *
     * @param columnNames The names of the columns.
     * @return A new {@link RegularViewSpecification}.
     * @see RegularViewSpecification
     */
    RegularViewSpecification createRegularViewSpecification( String... columnNames );
}
