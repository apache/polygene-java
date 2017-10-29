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

import java.util.Objects;
import org.apache.polygene.library.sql.generator.Typeable;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessor;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public abstract class AbstractProcessor<ProcessableType extends Typeable<?>>
    implements SQLProcessor
{

    private final Class<? extends ProcessableType> _type;

    public AbstractProcessor( Class<? extends ProcessableType> realType )
    {
        Objects.requireNonNull( realType, "Processable type" );
        this._type = realType;
    }

    public void process( SQLProcessorAggregator aggregator, Typeable<?> object, StringBuilder builder )
    {
        if( object != null )
        {
            this.doProcess( aggregator, this._type.cast( object ), builder );
        }
    }

    protected abstract void doProcess( SQLProcessorAggregator aggregator, ProcessableType object, StringBuilder builder );
}
