/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.entitystore.sql.internal;

import org.qi4j.entitystore.sql.internal.DatabaseSQLStringsBuilder.CommonMixin;
import org.sql.generation.api.grammar.common.datatypes.SQLDataType;
import org.sql.generation.api.vendor.PostgreSQLVendor;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLStringBuilderMixin extends CommonMixin
{

    @Override
    protected SQLDataType getIDType()
    {
        return ((PostgreSQLVendor) this.getVendor()).getDataTypeFactory().text();
    }

    @Override
    protected SQLDataType getStateType()
    {
        return ((PostgreSQLVendor) this.getVendor()).getDataTypeFactory().text();
    }
}
