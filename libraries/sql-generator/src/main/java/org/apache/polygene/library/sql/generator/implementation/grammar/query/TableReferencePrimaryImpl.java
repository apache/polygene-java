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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import org.apache.polygene.library.sql.generator.grammar.query.TableAlias;
import org.apache.polygene.library.sql.generator.grammar.query.TableReferencePrimary;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class TableReferencePrimaryImpl<TableReferenceType extends TableReferencePrimary> extends
                                                                                         TableReferenceImpl<TableReferencePrimary, TableReferenceType>
    implements TableReferencePrimary
{

    private final TableAlias _tableAlias;

    public TableReferencePrimaryImpl( SQLProcessorAggregator processor,
                                      Class<? extends TableReferenceType> tableReferenceClass, TableAlias alias )
    {
        super( processor, tableReferenceClass );
        this._tableAlias = alias;
    }

    public TableAlias getTableAlias()
    {
        return this._tableAlias;
    }

    @Override
    protected boolean doesEqual( TableReferenceType another )
    {
        return TypeableImpl.bothNullOrEquals( this._tableAlias, another.getTableAlias() );
    }
}
