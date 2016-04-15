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
package org.apache.zest.api.unitofwork.concern;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation describes the retries that should occur in case of
 * {@link org.apache.zest.api.unitofwork.ConcurrentEntityModificationException}
 * occurs.
 */
@Retention( RUNTIME )
@Target( METHOD )
@Inherited
@Documented
public @interface UnitOfWorkRetry
{
    /**
     * Number of times that the UnitOfWork should be tried in total.
     * The default value is 1, which means that the UnitOfWork will execute only one time. It is also the minimum
     * value allowed.
     *
     * @return Number of times that the UnitOfWork will be executed. Must be 1 or higher. If a value of 0 or lower is
     * given, the UnitOfWork is still executed one time.
     */
    int retries() default 1;

    /**
     * Number of milliseconds to wait before executing the second UnitOfOfWork.
     * The default value is 0, which means that there is no delay and it is tried immediately.
     *
     * @return Number of milliseconds to wait before executing the second UnitOfOfWork.
     */
    long initialDelay() default 0;

    /**
     * Number of milliseconds to be added for each additional retry, beyond the second one.
     * The default value is 10.
     *
     * The delay is defined as;
     *
     * <pre><code>
     *
     * Thread.sleep( initialDelay + retry * delayFactor );
     * </code></pre>
     * where retry will be 0 after first UnitOfWork had a {@link ConcurrentEntityModificationException} and is 1 after
     * the first retry and so forth.
     * <p>
     * So, with the {@code retries=4, initialDelay=5, delayFactor=20} the 3 delays between the UnitOfWorks will be
     * {@code 5ms, 25ms, 45ms}
     * </p>
     *
     * @return The number of milliseconds per retry, except the first one, that should be added to the delay between
     * tries.
     */
    long delayFactor() default 10;
}
