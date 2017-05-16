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

import org.apache.polygene.library.sql.generator.grammar.common.datatypes.SQLDataType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.AutoGenerationPolicy;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ConstraintCharacteristics;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.factories.DefinitionFactory;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLFactoryBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * @author Stanislav Muhametsin
 */
public abstract class AbstractDefinitionFactory extends SQLFactoryBase
    implements DefinitionFactory
{

    protected AbstractDefinitionFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType )
    {
        return this.createColumnDefinition( columnName, columnDataType, null, true );
    }

    public ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, Boolean mayBeNull )
    {
        return this.createColumnDefinition( columnName, columnDataType, null, mayBeNull );
    }

    public ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, String columnDefault )
    {
        return this.createColumnDefinition( columnName, columnDataType, columnDefault, true );
    }

    public ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType,
                                                    String columnDefault, Boolean mayBeNull )
    {
        return this.createColumnDefinition( columnName, columnDataType, columnDefault, mayBeNull, null );
    }

    public ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType, Boolean mayBeNull,
                                                    AutoGenerationPolicy autoGenerationPolicy )
    {
        return this.createColumnDefinition( columnName, columnDataType, null, mayBeNull, autoGenerationPolicy );
    }

    public TableConstraintDefinition createTableConstraintDefinition( String name, TableConstraint constraint )
    {
        return this.createTableConstraintDefinition( name, constraint, null );
    }

    public TableConstraintDefinition createTableConstraintDefinition( TableConstraint constraint )
    {
        return this.createTableConstraintDefinition( null, constraint, null );
    }

    public TableConstraintDefinition createTableConstraintDefinition( TableConstraint constraint,
                                                                      ConstraintCharacteristics characteristics )
    {
        return this.createTableConstraintDefinition( null, constraint, characteristics );
    }

    protected abstract ColumnDefinition createColumnDefinition( String columnName, SQLDataType columnDataType,
                                                                String columnDefault, Boolean mayBeNull, AutoGenerationPolicy autoGenerationPolicy );
}
