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

/**
 * This class represents {@code CHARACTER} and {@code CHARACTER VARYING} data types, typically abbreviated as
 * {@code CHAR} and {@code VARCHAR}, respectively.
 *
 * @author Stanislav Muhametsin
 */
public interface SQLChar
    extends SQLDataType, ParametrizableDataType
{

    /**
     * Returns {@code true} if this is {@code CHARACTER VARYING}; {@code false otherwise}.
     *
     * @return {@code true} if this is {@code CHARACTER VARYING}; {@code false otherwise}.
     */
    Boolean isVarying();

    /**
     * Returns the length specification for this {@code CHARACTER} or {@code CHARACTER VARYING}. Returns {@code null} if
     * none specified.
     *
     * @return The length for this {@code CHARACTER} or {@code CHARACTER VARYING}.
     */
    Integer getLength();
}