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
package org.apache.polygene.library.sql.generator.implementation.transformation.pgsql;

import java.util.Map;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.definition.table.AutoGenerationPolicy;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ColumnDefinition;
import org.apache.polygene.library.sql.generator.implementation.transformation.DefinitionProcessing.ColumnDefinitionProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class DefinitionProcessing
{
    public static class PGColumnDefinitionProcessor extends ColumnDefinitionProcessor
    {
        private final Map<Class<?>, String> _dataTypeSerialNames;

        public PGColumnDefinitionProcessor( Map<Class<?>, String> dataTypeSerialNames )
        {
            Objects.requireNonNull( dataTypeSerialNames, "Data type serial names" );
            this._dataTypeSerialNames = dataTypeSerialNames;
        }

        @Override
        protected void processDataType( SQLProcessorAggregator aggregator, ColumnDefinition object,
                                        StringBuilder builder )
        {
            AutoGenerationPolicy autoGenPolicy = object.getAutoGenerationPolicy();
            if( autoGenPolicy == null )
            {
                super.processDataType( aggregator, object, builder );
            }
            else
            {
                // PostgreSQL can't handle the ALWAYS strategy
                if( AutoGenerationPolicy.BY_DEFAULT.equals( autoGenPolicy ) )
                {
                    // Don't produce default data type if auto generated
                    Class<?> dtClass = object.getDataType().getClass();
                    Boolean success = false;
                    for( Map.Entry<Class<?>, String> entry : this._dataTypeSerialNames.entrySet() )
                    {
                        success = entry.getKey().isAssignableFrom( dtClass );
                        if( success )
                        {
                            builder.append( entry.getValue() );
                            break;
                        }
                    }
                    if( !success )
                    {
                        throw new UnsupportedOperationException( "Unsupported column data type " + object.getDataType()
                                                                 + " for auto-generated column." );
                    }
                }
                else
                {
                    throw new UnsupportedOperationException( "Unsupported auto generation policy: " + autoGenPolicy
                                                             + "." );
                }
            }
        }

        @Override
        protected void processAutoGenerationPolicy( ColumnDefinition object, StringBuilder builder )
        {
            // Nothing to do - auto generation policy handled in data type orc
        }
    }
}
