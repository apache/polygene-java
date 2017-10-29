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
package org.apache.polygene.library.sql.generator.implementation.vendor.pgsql;

import org.apache.polygene.library.sql.generator.grammar.factories.ModificationFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.pgsql.PgSQLDataTypeFactory;
import org.apache.polygene.library.sql.generator.grammar.factories.pgsql.PgSQLManipulationFactory;
import org.apache.polygene.library.sql.generator.implementation.grammar.factories.pgsql.PgSQLDataTypeFactoryImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.factories.pgsql.PgSQLManipulationFactoryImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.factories.pgsql.PgSQLModificationFactoryImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.pgsql.PostgreSQLProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.implementation.vendor.DefaultVendor;
import org.apache.polygene.library.sql.generator.vendor.PostgreSQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class PostgreSQLVendorImpl extends DefaultVendor
    implements PostgreSQLVendor
{
    protected static final Callback<PgSQLDataTypeFactory> PG_DATA_TYPE_FACTORY =
        new Callback<PgSQLDataTypeFactory>()
        {
            public PgSQLDataTypeFactory get( SQLVendor vendor, SQLProcessorAggregator processor )
            {
                return new PgSQLDataTypeFactoryImpl( (PostgreSQLVendor) vendor, processor );
            }
        };

    protected static final Callback<PgSQLManipulationFactory> PG_MANIPULATION_FACTORY =
        new Callback<PgSQLManipulationFactory>()
        {
            public PgSQLManipulationFactory
            get( SQLVendor vendor, SQLProcessorAggregator processor )
            {
                return new PgSQLManipulationFactoryImpl( (PostgreSQLVendor) vendor, processor );
            }
        };

    protected static final Callback<ModificationFactory> PG_MODIFICATION_FACTORY =
        new Callback<ModificationFactory>()
        {
            public ModificationFactory get( SQLVendor vendor, SQLProcessorAggregator processor )
            {
                return new PgSQLModificationFactoryImpl( (PostgreSQLVendor) vendor, processor );
            }
        };

    protected static final ProcessorCallback PG_PROCESSOR = new ProcessorCallback()
    {
        public SQLProcessorAggregator get( SQLVendor vendor )
        {
            return new PostgreSQLProcessor( vendor );
        }
    };

    private boolean _legacyOffsetAndLimit;

    public PostgreSQLVendorImpl()
    {
        super( PG_PROCESSOR, BOOLEAN_FACTORY, COLUMNS_FACTORY, LITERAL_FACTORY,
               PG_MODIFICATION_FACTORY, QUERY_FACTORY,
               TABLE_REFERENCE_FACTORY, DEFINITION_FACTORY, PG_MANIPULATION_FACTORY,
               PG_DATA_TYPE_FACTORY );
        this._legacyOffsetAndLimit = false;
    }

    @Override
    public PgSQLDataTypeFactory getDataTypeFactory()
    {
        return (PgSQLDataTypeFactory) super.getDataTypeFactory();
    }

    @Override
    public PgSQLManipulationFactory getManipulationFactory()
    {
        return (PgSQLManipulationFactory) super.getManipulationFactory();
    }

    public boolean legacyOffsetAndLimit()
    {
        return this._legacyOffsetAndLimit;
    }

    public void setLegacyOffsetAndLimit( boolean useLegacyOffsetAndLimit )
    {
        this._legacyOffsetAndLimit = useLegacyOffsetAndLimit;
    }
}
