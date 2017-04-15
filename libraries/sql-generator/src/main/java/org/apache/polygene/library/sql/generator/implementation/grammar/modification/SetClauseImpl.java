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
package org.apache.polygene.library.sql.generator.implementation.grammar.modification;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.modification.SetClause;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSource;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class SetClauseImpl extends SQLSyntaxElementBase<SetClause, SetClause>
    implements SetClause
{

    private final String _target;

    private final UpdateSource _source;

    public SetClauseImpl( SQLProcessorAggregator processor, String updateTarget, UpdateSource updateSource )
    {
        this( processor, SetClause.class, updateTarget, updateSource );
    }

    protected SetClauseImpl( SQLProcessorAggregator processor, Class<? extends SetClause> expressionClass,
                             String updateTarget, UpdateSource updateSource )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( updateTarget, "update target" );
        Objects.requireNonNull( updateSource, "source" );

        this._target = updateTarget;
        this._source = updateSource;
    }

    public UpdateSource getUpdateSource()
    {
        return this._source;
    }

    public String getUpdateTarget()
    {
        return this._target;
    }

    @Override
    protected boolean doesEqual( SetClause another )
    {
        return this._target.equals( another.getUpdateTarget() ) && this._source.equals( another.getUpdateSource() );
    }
}
