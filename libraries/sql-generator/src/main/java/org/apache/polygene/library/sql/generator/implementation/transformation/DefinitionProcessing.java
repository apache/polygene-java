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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.schema.SchemaElement;
import org.apache.polygene.library.sql.generator.grammar.definition.table.AutoGenerationPolicy;
import org.apache.polygene.library.sql.generator.grammar.definition.table.CheckConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ConstraintCharacteristics;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ForeignKeyConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.LikeClause;
import org.apache.polygene.library.sql.generator.grammar.definition.table.MatchType;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ReferentialAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableCommitAction;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElement;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElementList;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableScope;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.UniqueSpecification;
import org.apache.polygene.library.sql.generator.grammar.definition.view.RegularViewSpecification;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewCheckOption;
import org.apache.polygene.library.sql.generator.grammar.definition.view.ViewDefinition;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class DefinitionProcessing
{

    public static class SchemaDefinitionProcessor extends AbstractProcessor<SchemaDefinition>
    {
        public SchemaDefinitionProcessor()
        {
            super( SchemaDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, SchemaDefinition object, StringBuilder builder )
        {
            builder.append( SQLConstants.CREATE ).append( SQLConstants.TOKEN_SEPARATOR ).append( "SCHEMA " )
                   .append( object.getSchemaName() );
            String charset = object.getSchemaCharset();
            if( charset != null )
            {
                builder.append( " DEFAULT CHARSET " ).append( object.getSchemaCharset() );
            }

            builder.append( SQLConstants.NEWLINE );
        }

        protected void processSchemaElements( SQLProcessorAggregator aggregator, SchemaDefinition object,
                                              StringBuilder builder )
        {
            for( SchemaElement el : object.getSchemaElements() )
            {
                aggregator.process( el.asTypeable(), builder );
                builder.append( SQLConstants.NEWLINE );
            }
        }
    }

    public static class TableDefinitionProcessor extends AbstractProcessor<TableDefinition>
    {
        private static final Map<TableScope, String> _defaultTableScopes;
        private static final Map<TableCommitAction, String> _defaultCommitActions;

        static
        {
            Map<TableScope, String> operations = new HashMap<TableScope, String>();
            operations.put( TableScope.GLOBAL_TEMPORARY, "GLOBAL TEMPORARY" );
            operations.put( TableScope.LOCAL_TEMPORARY, "LOCAL TEMPORARY" );
            _defaultTableScopes = operations;

            Map<TableCommitAction, String> commitActions = new HashMap<TableCommitAction, String>();
            commitActions.put( TableCommitAction.ON_COMMIT_DELETE_ROWS, "DELETE ROWS" );
            commitActions.put( TableCommitAction.ON_COMMIT_PRESERVE_ROWS, "PRESERVE ROWS" );
            _defaultCommitActions = commitActions;
        }

        public static Map<TableCommitAction, String> getDefaultCommitActions()
        {
            return Collections.unmodifiableMap( _defaultCommitActions );
        }

        public static Map<TableScope, String> getDefaultTableScopes()
        {
            return Collections.unmodifiableMap( _defaultTableScopes );
        }

        private final Map<TableScope, String> _tableScopes;
        private final Map<TableCommitAction, String> _commitActions;

        public TableDefinitionProcessor()
        {
            this( _defaultTableScopes, _defaultCommitActions );
        }

        public TableDefinitionProcessor( Map<TableScope, String> tableScopes,
                                         Map<TableCommitAction, String> commitActions )
        {
            super( TableDefinition.class );

            this._tableScopes = tableScopes;
            this._commitActions = commitActions;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, TableDefinition object, StringBuilder builder )
        {
            builder.append( SQLConstants.CREATE );
            if( object.getTableScope() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( this._tableScopes.get( object.getTableScope() ) );
            }

            builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "TABLE" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getTableName(), builder );

            builder.append( SQLConstants.NEWLINE );
            aggregator.process( object.getContents(), builder );

            builder.append( SQLConstants.NEWLINE );
            if( object.getCommitAction() != null )
            {
                builder.append( "ON COMMIT" ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( this._commitActions.get( object.getCommitAction() ) );
            }
        }
    }

    public static class TableElementListProcessor extends AbstractProcessor<TableElementList>
    {

        public TableElementListProcessor()
        {
            super( TableElementList.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, TableElementList object, StringBuilder builder )
        {
            Iterator<TableElement> iter = object.getElementList().iterator();
            builder.append( SQLConstants.OPEN_PARENTHESIS ).append( SQLConstants.NEWLINE );
            while( iter.hasNext() )
            {
                this.processTableElement( aggregator, iter.next(), builder, iter.hasNext() );
                builder.append( SQLConstants.NEWLINE );
            }
            builder.append( SQLConstants.CLOSE_PARENTHESIS );
        }

        protected void processTableElement( SQLProcessorAggregator aggregator, TableElement object, StringBuilder builder, boolean hasNext )
        {
            aggregator.process( object, builder );
            if( hasNext )
            {
                builder.append( SQLConstants.COMMA );
            }
        }
    }

    public static class ColumnDefinitionProcessor extends AbstractProcessor<ColumnDefinition>
    {

        public ColumnDefinitionProcessor()
        {
            super( ColumnDefinition.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, ColumnDefinition object, StringBuilder builder )
        {
            builder.append( object.getColumnName() ).append( SQLConstants.TOKEN_SEPARATOR );

            this.processDataType( aggregator, object, builder );

            if( object.getDefault() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "DEFAULT" )
                       .append( SQLConstants.TOKEN_SEPARATOR ).append( object.getDefault() );
            }

            this.processMayBeNull( object, builder );

            if( object.getAutoGenerationPolicy() != null )
            {
                this.processAutoGenerationPolicy( object, builder );
            }
        }

        protected void processMayBeNull( ColumnDefinition object, StringBuilder builder )
        {
            if( !object.mayBeNull() )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "NOT NULL" );
            }
        }

        protected void processDataType( SQLProcessorAggregator aggregator, ColumnDefinition object,
                                        StringBuilder builder )
        {
            aggregator.process( object.getDataType(), builder );
        }

        protected void processAutoGenerationPolicy( ColumnDefinition object, StringBuilder builder )
        {
            builder.append( " GENERATED " );
            if( AutoGenerationPolicy.ALWAYS.equals( object.getAutoGenerationPolicy() ) )
            {
                builder.append( "ALWAYS " );
            }
            else if( AutoGenerationPolicy.BY_DEFAULT.equals( object.getAutoGenerationPolicy() ) )
            {
                builder.append( "BY DEFAULT " );
            }
            else
            {
                throw new UnsupportedOperationException( "Unknown auto generation policy: "
                                                         + object.getAutoGenerationPolicy() + "." );
            }
            builder.append( "AS IDENTITY" );
        }
    }

    public static class LikeClauseProcessor extends AbstractProcessor<LikeClause>
    {

        public LikeClauseProcessor()
        {
            super( LikeClause.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, LikeClause object, StringBuilder builder )
        {
            builder.append( "LIKE" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getTableName(), builder );
        }
    }

    public static class TableConstraintDefinitionProcessor extends AbstractProcessor<TableConstraintDefinition>
    {

        private static final Map<ConstraintCharacteristics, String> _defaultCharacteristics;

        static
        {
            Map<ConstraintCharacteristics, String> operations = new HashMap<ConstraintCharacteristics, String>();
            operations.put( ConstraintCharacteristics.INITIALLY_DEFERRED_DEFERRABLE, "INITIALLY DEFERRED DEFERRABLE" );
            operations.put( ConstraintCharacteristics.INITIALLY_IMMEDIATE_DEFERRABLE, "INITIALLY IMMEDIATE DEFERRABLE" );
            operations.put( ConstraintCharacteristics.NOT_DEFERRABLE, "NOT DEFERRABLE" );
            _defaultCharacteristics = operations;
        }

        private final Map<ConstraintCharacteristics, String> _characteristics;

        public TableConstraintDefinitionProcessor()
        {
            this( _defaultCharacteristics );
        }

        public TableConstraintDefinitionProcessor( Map<ConstraintCharacteristics, String> characteristics )
        {
            super( TableConstraintDefinition.class );

            this._characteristics = characteristics;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, TableConstraintDefinition object,
                                  StringBuilder builder )
        {
            if( object.getConstraintName() != null )
            {
                builder.append( "CONSTRAINT" ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( object.getConstraintName() ).append( SQLConstants.TOKEN_SEPARATOR );
            }

            aggregator.process( object.getConstraint(), builder );

            if( object.getCharacteristics() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append(
                    this._characteristics.get( object.getCharacteristics() ) );
            }
        }
    }

    public static class CheckConstraintProcessor extends AbstractProcessor<CheckConstraint>
    {

        public CheckConstraintProcessor()
        {
            super( CheckConstraint.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, CheckConstraint object, StringBuilder builder )
        {
            builder.append( "CHECK" ).append( SQLConstants.TOKEN_SEPARATOR ).append( SQLConstants.OPEN_PARENTHESIS );
            aggregator.process( object.getCheckCondition(), builder );
            builder.append( SQLConstants.CLOSE_PARENTHESIS );
        }
    }

    public static class UniqueConstraintProcessor extends AbstractProcessor<UniqueConstraint>
    {

        private static final Map<UniqueSpecification, String> _defaultUniqueSpecs;

        static
        {
            Map<UniqueSpecification, String> map = new HashMap<UniqueSpecification, String>();
            map.put( UniqueSpecification.PRIMARY_KEY, "PRIMARY KEY" );
            map.put( UniqueSpecification.UNIQUE, "UNIQUE" );
            _defaultUniqueSpecs = map;
        }

        private final Map<UniqueSpecification, String> _uniqueSpecs;

        public UniqueConstraintProcessor()
        {
            this( _defaultUniqueSpecs );
        }

        public UniqueConstraintProcessor( Map<UniqueSpecification, String> uniqueSpecs )
        {
            super( UniqueConstraint.class );

            this._uniqueSpecs = uniqueSpecs;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, UniqueConstraint object, StringBuilder builder )
        {
            this.processUniqueness( aggregator, object, builder );
        }

        protected void processUniqueness( SQLProcessorAggregator aggregator, UniqueConstraint object, StringBuilder builder )
        {
            builder.append( this._uniqueSpecs.get( object.getUniquenessKind() ) );
            aggregator.process( object.getColumnNameList(), builder );
        }
    }

    public static class ForeignKeyConstraintProcessor extends AbstractProcessor<ForeignKeyConstraint>
    {
        private static final Map<ReferentialAction, String> _defaultReferentialActions;

        private static final Map<MatchType, String> _defaultMatchTypes;

        static
        {
            Map<ReferentialAction, String> map = new HashMap<ReferentialAction, String>();
            map.put( ReferentialAction.CASCADE, "CASCADE" );
            map.put( ReferentialAction.NO_ACTION, "NO ACTION" );
            map.put( ReferentialAction.RESTRICT, "RESTRICT" );
            map.put( ReferentialAction.SET_DEFAULT, "SET DEFAULT" );
            map.put( ReferentialAction.SET_NULL, "SET NULL" );
            _defaultReferentialActions = map;

            Map<MatchType, String> mt = new HashMap<MatchType, String>();
            mt.put( MatchType.FULL, "FULL" );
            mt.put( MatchType.PARTIAL, "PARTIAL" );
            mt.put( MatchType.SIMPLE, "SIMPLE" );
            _defaultMatchTypes = mt;
        }

        private final Map<ReferentialAction, String> _referentialActions;

        private final Map<MatchType, String> _matchTypes;

        public ForeignKeyConstraintProcessor()
        {
            this( _defaultReferentialActions, _defaultMatchTypes );
        }

        public ForeignKeyConstraintProcessor( Map<ReferentialAction, String> uniqueSpecs,
                                              Map<MatchType, String> matchTypes )
        {
            super( ForeignKeyConstraint.class );

            this._referentialActions = uniqueSpecs;
            this._matchTypes = matchTypes;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, ForeignKeyConstraint object, StringBuilder builder )
        {
            builder.append( "FOREIGN KEY" );
            aggregator.process( object.getSourceColumns(), builder );
            builder.append( SQLConstants.NEWLINE ).append( "REFERENCES" ).append( SQLConstants.TOKEN_SEPARATOR );
            aggregator.process( object.getTargetTableName(), builder );
            if( object.getTargetColumns() != null )
            {
                aggregator.process( object.getTargetColumns(), builder );
            }

            if( object.getMatchType() != null )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( "MATCH" ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( this._matchTypes.get( object.getMatchType() ) );
            }
            builder.append( SQLConstants.NEWLINE );

            this.handleReferentialAction( "ON UPDATE", object.getOnUpdate(), builder );
            builder.append( SQLConstants.TOKEN_SEPARATOR );
            this.handleReferentialAction( "ON DELETE", object.getOnDelete(), builder );
        }

        protected void handleReferentialAction( String prefix, ReferentialAction action, StringBuilder builder )
        {
            if( action != null )
            {
                builder.append( prefix ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( this._referentialActions.get( action ) );
            }
        }
    }

    public static class ViewDefinitionProcessor extends AbstractProcessor<ViewDefinition>
    {

        private static final Map<ViewCheckOption, String> _defaultViewCheckOptions;

        static
        {
            Map<ViewCheckOption, String> map = new HashMap<ViewCheckOption, String>();
            map.put( ViewCheckOption.CASCADED, "CASCADED" );
            map.put( ViewCheckOption.LOCAL, "LOCAL" );
            _defaultViewCheckOptions = map;
        }

        private final Map<ViewCheckOption, String> _viewCheckOptions;

        public ViewDefinitionProcessor()
        {
            this( _defaultViewCheckOptions );
        }

        public ViewDefinitionProcessor( Map<ViewCheckOption, String> viewCheckOptions )
        {
            super( ViewDefinition.class );

            this._viewCheckOptions = viewCheckOptions;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, ViewDefinition object, StringBuilder builder )
        {
            builder.append( SQLConstants.CREATE ).append( SQLConstants.TOKEN_SEPARATOR );
            if( object.isRecursive() )
            {
                builder.append( "RECURSIVE" ).append( SQLConstants.TOKEN_SEPARATOR );
            }
            builder.append( "VIEW" ).append( SQLConstants.TOKEN_SEPARATOR );

            aggregator.process( object.getViewName(), builder );
            aggregator.process( object.getViewSpecification(), builder );
            builder.append( "AS" ).append( SQLConstants.NEWLINE );
            aggregator.process( object.getViewQuery(), builder );

            if( object.getViewCheckOption() != null )
            {
                builder.append( SQLConstants.NEWLINE ).append( "WITH" ).append( SQLConstants.TOKEN_SEPARATOR )
                       .append( this._viewCheckOptions.get( object.getViewCheckOption() ) )
                       .append( SQLConstants.TOKEN_SEPARATOR ).append( "CHECK OPTION" );
            }
        }
    }

    public static class RegularViewSpecificationProcessor extends AbstractProcessor<RegularViewSpecification>
    {

        public RegularViewSpecificationProcessor()
        {
            super( RegularViewSpecification.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, RegularViewSpecification object,
                                  StringBuilder builder )
        {
            if( object.getColumns() != null )
            {
                aggregator.process( object.getColumns(), builder );
            }
        }
    }
}
