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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.definition;

import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.library.sql.generator.grammar.builders.definition.TableElementListBuilder;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElement;
import org.apache.polygene.library.sql.generator.grammar.definition.table.TableElementList;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.grammar.definition.table.TableElementListImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class TableElementListBuilderImpl extends SQLBuilderBase
    implements TableElementListBuilder
{

    private final List<TableElement> _elements;

    public TableElementListBuilderImpl( SQLProcessorAggregator processor )
    {
        super( processor );
        this._elements = new ArrayList<TableElement>();
    }

    public TableElementList createExpression()
    {
        return new TableElementListImpl( this.getProcessor(), this._elements );
    }

    public TableElementListBuilder addTableElement( TableElement element )
    {
        this._elements.add( element );
        return this;
    }

    public List<TableElement> getTableElements()
    {
        return this._elements;
    }
}
