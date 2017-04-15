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
 * This class contains all the types for fields in {@code INTERVAL} data type.
 *
 * @author Stanislav Muhametsin
 */
public final class IntervalDataType
{
    public static final IntervalDataType YEAR = new IntervalDataType();

    public static final IntervalDataType MONTH = new IntervalDataType();

    public static final IntervalDataType DAY = new IntervalDataType();

    public static final IntervalDataType HOUR = new IntervalDataType();

    public static final IntervalDataType MINUTE = new IntervalDataType();

    public static final IntervalDataType SECOND = new IntervalDataType();
}