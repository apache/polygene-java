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
package org.apache.polygene.library.sql.generator.grammar.common.datatypes;

import org.apache.polygene.library.sql.generator.grammar.factories.DataTypeFactory;

/**
 * This is a general way of handling any user-created or otherwise custom type. It is advisable to always use the
 * methods provided by your vendor's data type factory (most likely sub-interface of {@link DataTypeFactory}) instead of
 * using this interface. However, when data type is dynamically created, it is quite a must to use this type.
 *
 */
public interface UserDefinedType
    extends SQLDataType
{

    String getTextualRepresentation();
}
