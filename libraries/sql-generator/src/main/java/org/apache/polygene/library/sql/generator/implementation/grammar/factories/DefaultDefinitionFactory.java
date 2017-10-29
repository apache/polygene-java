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
package org.apache.polygene.library.sql.generator.implementation.grammar.factories;

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
import org.apache.polygene.library.sql.generator.grammar.definition.table.LikeClause;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.view.RegularViewSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.ForeignKeyConstraintBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.SchemaDefinitionBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.TableDefinitionBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.TableElementListBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.UniqueConstraintBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition.ViewDefinitionBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.CheckConstraintImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.ColumnDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.LikeClauseImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.TableConstraintDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.view.RegularViewSpecificationImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultDefinitionFactory extends AbstractDefinitionFactory
{
    public DefaultDefinitionFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public SchemaDefinitionBuilder createSchemaDefinitionBuilder()
    {
        return new SchemaDefinitionBuilderImpl( this.getProcessor() );
    }

    public TableDefinitionBuilder createTableDefinitionBuilder()
    {
        return new TableDefinitionBuilderImpl( this.getProcessor() );
    }

    public TableElementListBuilder createTableElementListBuilder()
    {
        return new TableElementListBuilderImpl( this.getProcessor() );
    }

    @Override
    protected ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType,
                                                       String columnDefault, Boolean mayBeNull, AutoGenerationPolicy autoGenerationPolicy )
    {
        return new ColumnDefinitionImpl( this.getProcessor(), columnName, columnDataType, columnDefault, mayBeNull,
                                         autoGenerationPolicy );
    }

    public LikeClause createLikeClause( TableNameDirect tableName )
    {
        return new LikeClauseImpl( this.getProcessor(), tableName );
    }

    public TableConstraintDefinition createTableConstraintDefinition( String name, TableConstraint constraint,
                                                                      ConstraintCharacteristics characteristics )
    {
        return new TableConstraintDefinitionImpl( this.getProcessor(), name, constraint, characteristics );
    }

    public CheckConstraint createCheckConstraint( BooleanExpression check )
    {
        return new CheckConstraintImpl( this.getProcessor(), check );
    }

    public UniqueConstraintBuilder createUniqueConstraintBuilder()
    {
        return new UniqueConstraintBuilderImpl( this.getProcessor(), this.getVendor().getColumnsFactory() );
    }

    public ForeignKeyConstraintBuilder createForeignKeyConstraintBuilder()
    {
        return new ForeignKeyConstraintBuilderImpl( this.getProcessor(), this.getVendor().getColumnsFactory() );
    }

    public ViewDefinitionBuilder createViewDefinitionBuilder()
    {
        return new ViewDefinitionBuilderImpl( this.getProcessor() );
    }

    public RegularViewSpecification createRegularViewSpecification( String... columnNames )
    {
        return new RegularViewSpecificationImpl( this.getProcessor(), this.getVendor().getColumnsFactory()
                                                                          .colNames( columnNames ) );
    }
}
