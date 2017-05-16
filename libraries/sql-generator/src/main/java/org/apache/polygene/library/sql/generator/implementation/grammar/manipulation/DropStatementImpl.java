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
import org.apache.polygene.library.sql.generator.grammar.common.SchemaStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropBehaviour;
import org.apache.polygene.library.sql.generator.grammar.manipulation.DropStatement;
import org.apache.polygene.library.sql.generator.grammar.manipulation.ObjectType;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class DropStatementImpl<DropStatementType extends DropStatement> extends
                                                                        SQLSyntaxElementBase<SchemaStatement, DropStatementType>
    implements DropStatement
{

    private final DropBehaviour _dropBehaviour;
    private final ObjectType _whatToDrop;

    protected DropStatementImpl( SQLProcessorAggregator processor,
                                 Class<? extends DropStatementType> realImplementingType, ObjectType whatToDrop, DropBehaviour dropBehaviour )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( whatToDrop, "What to drop" );
        Objects.requireNonNull( dropBehaviour, "Drop behaviour" );
        this._whatToDrop = whatToDrop;
        this._dropBehaviour = dropBehaviour;
    }

    protected boolean doesEqual( DropStatementType another )
    {
        return this._dropBehaviour.equals( another.getDropBehaviour() )
               && this._whatToDrop.equals( another.whatToDrop() );
    }

    public DropBehaviour getDropBehaviour()
    {
        return this._dropBehaviour;
    }

    public ObjectType whatToDrop()
    {
        return this._whatToDrop;
    }
}
