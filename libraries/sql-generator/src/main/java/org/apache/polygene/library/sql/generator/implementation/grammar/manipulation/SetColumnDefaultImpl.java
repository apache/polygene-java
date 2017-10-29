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
package org.apache.polygene.library.sql.generator.implementation.grammar.manipulation;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterColumnAction;
import org.apache.polygene.library.sql.generator.grammar.manipulation.SetColumnDefault;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class SetColumnDefaultImpl extends SQLSyntaxElementBase<AlterColumnAction, SetColumnDefault>
    implements SetColumnDefault
{

    private final String _default;

    public SetColumnDefaultImpl( SQLProcessorAggregator processor, String colDefault )
    {
        this( processor, SetColumnDefault.class, colDefault );
    }

    protected SetColumnDefaultImpl( SQLProcessorAggregator processor,
                                    Class<? extends SetColumnDefault> realImplementingType, String colDefault )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( colDefault, "Column default" );
        this._default = colDefault;
    }

    @Override
    protected boolean doesEqual( SetColumnDefault another )
    {
        return this._default.equals( another.getDefault() );
    }

    public String getDefault()
    {
        return this._default;
    }
}
