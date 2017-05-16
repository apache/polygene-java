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
package org.apache.polygene.library.sql.generator.implementation.transformation;

import java.util.HashMap;
import java.util.Map;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AddColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AddTableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnAction.DropDefault;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropSchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropTableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropTableOrViewStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.ObjectType;
import org.apache.polygene.library.sql.generator.grammar.manipulation.SetColumnDefault;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class ManipulationProcessing
{

    public static class AlterTableStatementProcessor extends AbstractProcessor<AlterTableStatement>
    {
        public AlterTableStatementProcessor()
        {
            super( AlterTableStatement.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, AlterTableStatement object, StringBuilder builder )
        {
            builder.append( "ALTER TABLE" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getTableName(), builder );
            builder.append( SQLConstants.NEWLINE );
            aggregator.process( object.getAction(), builder );
        }
    }

    public static class AddColumnDefinitionProcessor extends AbstractProcessor<AddColumnDefinition>
    {

        public AddColumnDefinitionProcessor()
        {
            super( AddColumnDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, AddColumnDefinition object, StringBuilder builder )
        {
            builder.append( "ADD COLUMN" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getColumnDefinition(), builder );
        }
    }

    public static class AddTableConstraintDefinitionProcessor extends AbstractProcessor<AddTableConstraintDefinition>
    {

        public AddTableConstraintDefinitionProcessor()
        {
            super( AddTableConstraintDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, AddTableConstraintDefinition object,
                                  StringBuilder builder )
        {
            builder.append( "ADD" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getConstraint(), builder );
        }
    }

    public static class AlterColumnDefinitionProcessor extends AbstractProcessor<AlterColumnDefinition>
    {
        public AlterColumnDefinitionProcessor()
        {
            super( AlterColumnDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, AlterColumnDefinition object, StringBuilder builder )
        {
            builder.append( "ALTER COLUMN" ).append( SQLConstants.TOKEN_SEPARATOR ).append( object.getColumnName() )
                   .append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getAction(), builder );
        }
    }

    public static class DropColumnDefaultProcessor extends AbstractProcessor<DropDefault>
    {
        public DropColumnDefaultProcessor()
        {
            super( DropDefault.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, DropDefault object, StringBuilder builder )
        {
            builder.append( "DROP DEFAULT" );
        }
    }

    public static class SetColumnDefaultProcessor extends AbstractProcessor<SetColumnDefault>
    {
        public SetColumnDefaultProcessor()
        {
            super( SetColumnDefault.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SetColumnDefault object, StringBuilder builder )
        {
            builder.append( "SET" ).append( SQLConstants.TOKEN_SEPARATOR );
            builder.append( object.getDefault() );
        }
    }

    public static class DropColumnDefinitionProcessor extends AbstractProcessor<DropColumnDefinition>
    {
        public DropColumnDefinitionProcessor()
        {
            super( DropColumnDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, DropColumnDefinition object, StringBuilder builder )
        {
            builder.append( "DROP COLUMN" ).append( SQLConstants.TOKEN_SEPARATOR ).append( object.getColumnName() );
            ProcessorUtils.processDropBehaviour( object.getDropBehaviour(), builder );
        }
    }

    public static class DropTableConstraintDefinitionProcessor extends AbstractProcessor<DropTableConstraintDefinition>
    {
        public DropTableConstraintDefinitionProcessor()
        {
            super( DropTableConstraintDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, DropTableConstraintDefinition object,
                                  StringBuilder builder )
        {
            builder.append( "DROP CONSTRAINT" ).append( SQLConstants.TOKEN_SEPARATOR )
                   .append( object.getConstraintName() );
            ProcessorUtils.processDropBehaviour( object.getDropBehaviour(), builder );
        }
    }

    public static class DropSchemaStatementProcessor extends AbstractProcessor<DropSchemaStatement>
    {
        public DropSchemaStatementProcessor()
        {
            super( DropSchemaStatement.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, DropSchemaStatement object, StringBuilder builder )
        {
            builder.append( "DROP SCHEMA" ).append( SQLConstants.TOKEN_SEPARATOR ).append( object.getSchemaName() );
            ProcessorUtils.processDropBehaviour( object.getDropBehaviour(), builder );
        }
    }

    public static class DropTableOrViewStatementProcessor extends AbstractProcessor<DropTableOrViewStatement>
    {
        private static final Map<ObjectType, String> _defaultObjectTypes;

        static
        {
            Map<ObjectType, String> map = new HashMap<ObjectType, String>();
            map.put( ObjectType.TABLE, "TABLE" );
            map.put( ObjectType.VIEW, "VIEW" );

            _defaultObjectTypes = map;
        }

        private final Map<ObjectType, String> _objectTypes;

        public DropTableOrViewStatementProcessor()
        {
            this( _defaultObjectTypes );
        }

        public DropTableOrViewStatementProcessor( Map<ObjectType, String> objectTypes )
        {
            super( DropTableOrViewStatement.class );

            this._objectTypes = objectTypes;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, DropTableOrViewStatement object,
                                  StringBuilder builder )
        {
            builder.append( "DROP" ).append( SQLConstants.TOKEN_SEPARATOR )
                   .append( this._objectTypes.get( object.whatToDrop() ) ).append( SQLConstants.TOKEN_SEPARATOR );

            aggregator.process( object.getTableName(), builder );

            ProcessorUtils.processDropBehaviour( object.getDropBehaviour(), builder );
        }

        protected Map<ObjectType, String> getObjectTypes()
        {
            return this._objectTypes;
        }
    }
}
