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
package org.apache.polygene.library.sql.generator.implementation.grammar.common;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * @author 2011 Stanislav Muhametsin
 */
public abstract class SQLFactoryBase
{

    private final SQLVendor _vendor;
    private final SQLProcessorAggregator _processor;

    protected SQLFactoryBase( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        Objects.requireNonNull( vendor, "vendor" );
        Objects.requireNonNull( processor, "SQL processor" );

        this._vendor = vendor;
        this._processor = processor;
    }

    protected SQLVendor getVendor()
    {
        return this._vendor;
    }

    protected SQLProcessorAggregator getProcessor()
    {
        return this._processor;
    }
}
