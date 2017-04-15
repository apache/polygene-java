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

import org.apache.polygene.library.sql.generator.grammar.common.TableNameDirect;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.factories.ManipulationFactory;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AddColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AddTableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropSchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropTableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropTableOrViewStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.ObjectType;
import org.apache.polygene.library.sql.generator.grammar.manipulation.SetColumnDefault;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLFactoryBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.AddColumnDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.AddTableConstraintDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.AlterColumnDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.AlterTableStatementImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.DropColumnDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.DropSchemaStatementImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.DropTableConstraintDefinitionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.DropTableOrViewStatementImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.manipulation.SetColumnDefaultImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * @author Stanislav Muhametsin
 */
public class DefaultManipulationFactory extends SQLFactoryBase
    implements ManipulationFactory
{

    public DefaultManipulationFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public AlterTableStatement createAlterTableStatement( TableNameDirect tableName, AlterTableAction action )
    {
        return new AlterTableStatementImpl( this.getProcessor(), tableName, action );
    }

    public AddColumnDefinition createAddColumnDefinition( ColumnDefinition definition )
    {
        return new AddColumnDefinitionImpl( this.getProcessor(), definition );
    }

    public AddTableConstraintDefinition createAddTableConstraintDefinition(
        TableConstraintDefinition constraintDefinition )
    {
        return new AddTableConstraintDefinitionImpl( this.getProcessor(), constraintDefinition );
    }

    public AlterColumnDefinition createAlterColumnDefinition( String columnName, AlterColumnAction action )
    {
        return new AlterColumnDefinitionImpl( this.getProcessor(), columnName, action );
    }

    public SetColumnDefault createSetColumnDefault( String newDefault )
    {
        return new SetColumnDefaultImpl( this.getProcessor(), newDefault );
    }

    public DropColumnDefinition createDropColumnDefinition( String columnName, DropBehaviour dropBehaviour )
    {
        return new DropColumnDefinitionImpl( this.getProcessor(), columnName, dropBehaviour );
    }

    public DropTableConstraintDefinition createDropTableConstraintDefinition( String constraintName,
                                                                              DropBehaviour dropBehaviour )
    {
        return new DropTableConstraintDefinitionImpl( this.getProcessor(), constraintName, dropBehaviour );
    }

    public DropSchemaStatement createDropSchemaStatement( String schemaName, DropBehaviour dropBehaviour )
    {
        return new DropSchemaStatementImpl( this.getProcessor(), dropBehaviour, schemaName );
    }

    public DropTableOrViewStatement createDropTableOrViewStatement( TableNameDirect tableName, ObjectType theType,
                                                                    DropBehaviour dropBehaviour )
    {
        DropTableOrViewStatement result = null;
        if( theType == ObjectType.TABLE || theType == ObjectType.VIEW )
        {
            result = new DropTableOrViewStatementImpl( this.getProcessor(), theType, dropBehaviour, tableName );
        }
        else
        {
            throw new IllegalArgumentException( "Object type " + theType
                                                + " is not a valid type for DROP TABLE or DROP VIEW statement." );
        }

        return result;
    }
}
