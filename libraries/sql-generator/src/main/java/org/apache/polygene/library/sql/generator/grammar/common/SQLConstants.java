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
package org.apache.polygene.library.sql.generator.grammar.common;

/**
 * A container for common textual constants of SQL language.
 *
 * @author Stanislav Muhametsin
 */
public interface SQLConstants
{
    String SELECT = "SELECT";

    String FROM = "FROM";

    String WHERE = "WHERE";

    String GROUP_BY = "GROUP BY";

    String HAVING = "HAVING";

    String ORDER_BY = "ORDER BY";

    String TABLE_COLUMN_SEPARATOR = ".";

    String SCHEMA_TABLE_SEPARATOR = ".";

    String TOKEN_SEPARATOR = " ";

    String AND = "AND";

    String OR = "OR";

    String NOT = "NOT";

    String ASTERISK = "*";

    String COMMA = ",";

    String PERIOD = ".";

    String QUESTION_MARK = "?";

    String OPEN_PARENTHESIS = "(";

    String CLOSE_PARENTHESIS = ")";

    String ALIAS_DEFINER = "AS";

    String NEWLINE = "\n";

    String NULL = "NULL";

    String IS = "IS";

    String CREATE = "CREATE";

    String OFFSET_PREFIX = "OFFSET";

    String OFFSET_POSTFIX = "ROWS";

    String LIMIT_PREFIX = "FETCH FIRST";

    String LIMIT_POSTFIX = "ROWS ONLY";
}
