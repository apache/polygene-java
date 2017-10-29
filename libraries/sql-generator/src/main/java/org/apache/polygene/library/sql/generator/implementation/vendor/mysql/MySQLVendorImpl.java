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
package org.apache.polygene.library.sql.generator.implementation.vendor.mysql;

import org.apache.polygene.library.sql.generator.implementation.transformation.mysql.MySQLProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.implementation.vendor.DefaultVendor;
import org.apache.polygene.library.sql.generator.vendor.MySQLVendor;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class MySQLVendorImpl extends DefaultVendor
    implements MySQLVendor
{

    protected static final ProcessorCallback MYSQL_PROCESSOR = new ProcessorCallback()
    {
        public SQLProcessorAggregator get( SQLVendor vendor )
        {
            return new MySQLProcessor( vendor );
        }
    };

    private boolean _legacyLimit;

    public MySQLVendorImpl()
    {
        super( MYSQL_PROCESSOR );

        this._legacyLimit = false;
    }

    public boolean legacyLimit()
    {
        return this._legacyLimit;
    }

    public void setLegacyLimit( boolean useLegacyLimit )
    {
        this._legacyLimit = useLegacyLimit;
    }
}
