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
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AddTableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.manipulation.AlterTableAction;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class AddTableConstraintDefinitionImpl extends
                                              SQLSyntaxElementBase<AlterTableAction, AddTableConstraintDefinition>
    implements AddTableConstraintDefinition
{

    private final TableConstraintDefinition _constraint;

    public AddTableConstraintDefinitionImpl( SQLProcessorAggregator processor, TableConstraintDefinition constraint )
    {
        this( processor, AddTableConstraintDefinition.class, constraint );
    }

    protected AddTableConstraintDefinitionImpl( SQLProcessorAggregator processor,
                                                Class<? extends AddTableConstraintDefinition> realImplementingType, TableConstraintDefinition constraint )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( constraint, "Constraint" );
        this._constraint = constraint;
    }

    @Override
    protected boolean doesEqual( AddTableConstraintDefinition another )
    {
        return this._constraint.equals( another.getConstraint() );
    }

    public TableConstraintDefinition getConstraint()
    {
        return this._constraint;
    }
}
