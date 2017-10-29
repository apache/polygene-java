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
package org.apache.polygene.library.sql.generator.grammar.builders.definition;

import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.AbstractBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ForeignKeyConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.MatchType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ReferentialAction;

/**
 * The builder for table constraint {@code FOREIGN KEY(source columns) REFERENCES table_name(target columns) etc....}).
 *
 */
public interface ForeignKeyConstraintBuilder
    extends AbstractBuilder<ForeignKeyConstraint>
{

    /**
     * Adds source column names to this foreign key constraint.
     *
     * @param columnNames The source column names to be added to this foreign key constraint.
     * @return This builder.
     */
    ForeignKeyConstraintBuilder addSourceColumns( String... columnNames );

    /**
     * Adds target column names to this foreign key constraint.
     *
     * @param columnNames The target column names to be added to this foreign key constraint.
     * @return This builder.
     */
    ForeignKeyConstraintBuilder addTargetColumns( String... columnNames );

    /**
     * Sets the target table name for this foreign key constraint.
     *
     * @param tableName The target table name for this foreign key constraint.
     * @return This builder.
     */
    ForeignKeyConstraintBuilder setTargetTableName( TableNameDirect tableName );

    /**
     * Sets the match type for this foreign key constraint.
     *
     * @param matchType The match type for this foreign key constraint.
     * @return This builder.
     * @see MatchType
     */
    ForeignKeyConstraintBuilder setMatchType( MatchType matchType );

    /**
     * Sets the {@code ON UPDATE} action.
     *
     * @param action The action to perform {@code ON UPDATE}.
     * @return This builder.
     * @see ReferentialAction
     */
    ForeignKeyConstraintBuilder setOnUpdate( ReferentialAction action );

    /**
     * Sets the {@code ON DELETE} action.
     *
     * @param action The action to perform {@code ON DELETE}.
     * @return This builder.
     * @see ReferentialAction
     */
    ForeignKeyConstraintBuilder setOnDelete( ReferentialAction action );

    /**
     * Returns the source column names for this foreign key constraint.
     *
     * @return The source column names for this foreign key constraint.
     */
    List<String> getSourceColumns();

    /**
     * Returns the target column names for this foreign key constraint.
     *
     * @return The target column names for this foreign key constraint.
     */
    List<String> getTargetColumns();

    /**
     * Returns the target table name for this foreign key constraint.
     *
     * @return The target table name for this foreign key constraint.
     */
    TableNameDirect getTableName();

    /**
     * Returns the match type for this foreign key constraint.
     *
     * @return The match type for this foreign key constraint.
     * @see MatchType
     */
    MatchType getMatchType();

    /**
     * Returns the {@code ON UPDATE} action for this foreign key constraint.
     *
     * @return The {@code ON UPDATE} action for this foreign key constraint.
     * @see ReferentialAction
     */
    ReferentialAction getOnUpdate();

    /**
     * Returns the {@code ON DELETE} action for this foreign key constraint.
     *
     * @return The {@code ON DELETE} action for this foreign key constraint.
     * @see ReferentialAction
     */
    ReferentialAction getOnDelete();
}
