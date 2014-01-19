/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.sql.support.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The annotation, which tells the parameters for SQL type. Only applyable to certain typed properties, like for now,
 * {@link String}, {@link BigInteger}, and {@link BigDecimal}.
 *
 * Example of usage, where we want to limit the datatype of column storing this value in RDBMS to be 100 characters of
 * max length:
 * <pre>@SQLTypeInfo(maxLength=100)
 * public Property&lt;String&gt; myStringProperty();</pre>
 *
 * The purpose of this class is to optimize performance for people who know for certain that they will be using
 * SQL-Indexing in their application.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface SQLTypeInfo
{
    /**
     * For {@link String}s, this is the max character length as specified by {@code VARCHAR(length)}. For {@link BigInteger}s and {@link BigDecimal}s,
     * this is the precision of a {@code NUMERIC}.
     * @return Maximum length for SQL data type representing some property type.
     */
    int maxLength();

    /**
     * For {@link String}s and {@link BigInteger}s, this value is ignored. For {@link BigDecimal}s, this is the scale of {@code NUMERIC}, default being {@code 50}.
     *
     * @return The scale of SQL data type representing some property type.
     */
    int scale() default 50;
}
