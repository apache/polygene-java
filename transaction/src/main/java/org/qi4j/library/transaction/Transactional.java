/*
 * Copyright 2007 Rickard Öberg. All Rights Reserved.
 * Copyright 2007 Alin Dreghiciu. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.transaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Example transaction attribute.
 * <p/>
 * Note: preferably one should use the Spring tx annotations instead!
 * This is just as a sample of how tx support could work.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
@Inherited
@Documented
public @interface Transactional
{
    Propagation value() default Propagation.REQUIRED;

    /**
     * Defines how to deal with propagation of existing transaction or creation of a new transaction.
     * Similar to EJB CMT propagation.
     */
    public enum Propagation
    {

        /**
         * Default propagation behavior.
         * Behavior: <br>
         * If no current transaction: creates a new transaction <br>
         * If there is a current transaction: use the current transaction.
         */
        REQUIRED,

        /**
         * Behavior: <br>
         * If no current transaction: executes non transactionaly <br>
         * If there is a current transaction: use the current transaction.
         */
        SUPPORTS,

        /**
         * Behavior: <br>
         * If no current transaction: throw an exception <br>
         * If there is a current transaction: use the current transaction.
         */
        MANDATORY,

        /**
         * Behavior: <br>
         * If no current transaction: creates a new transaction <br>
         * If there is a current transaction: suspend the current transaction and create a new transaction.
         */
        REQUIRES_NEW,

        /**
         * Behavior: <br>
         * If no current transaction: executes non transactionaly <br>
         * If there is a current transaction: suspend the current transaction and executes non transactionaly.
         */
        NOT_SUPPORTED,

        /**
         * Behavior: <br>
         * If no current transaction: executes non transactionaly <br>
         * If there is a current transaction: throw an exception.
         */
        NEVER
    }
}
