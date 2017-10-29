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
package org.apache.polygene.library.sql.generator.implementation.grammar.definition.table;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.definition.table.ConstraintCharacteristics;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraint;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableConstraintDefinition;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElement;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableConstraintDefinitionImpl extends SQLSyntaxElementBase<TableElement, TableConstraintDefinition>
    implements TableConstraintDefinition
{

    private final String _name;
    private final TableConstraint _constraint;
    private final ConstraintCharacteristics _characteristics;

    public TableConstraintDefinitionImpl( SQLProcessorAggregator processor, String name, TableConstraint constraint,
                                          ConstraintCharacteristics characteristics )
    {
        this( processor, TableConstraintDefinition.class, name, constraint, characteristics );
    }

    protected TableConstraintDefinitionImpl( SQLProcessorAggregator processor,
                                             Class<? extends TableConstraintDefinition> realImplementingType, String name, TableConstraint constraint,
                                             ConstraintCharacteristics characteristics )
    {
        super( processor, realImplementingType );

        Objects.requireNonNull( constraint, "Constraint" );

        this._constraint = constraint;
        this._name = name;
        this._characteristics = characteristics;
    }

    @Override
    protected boolean doesEqual( TableConstraintDefinition another )
    {
        return this._constraint.equals( another.getConstraint() )
               && TypeableImpl.bothNullOrEquals( this._name, another.getConstraintName() )
               && TypeableImpl.bothNullOrEquals( this._characteristics, another.getCharacteristics() );
    }

    public ConstraintCharacteristics getCharacteristics()
    {
        return this._characteristics;
    }

    public TableConstraint getConstraint()
    {
        return this._constraint;
    }

    public String getConstraintName()
    {
        return this._name;
    }
}
