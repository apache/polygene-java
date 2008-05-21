/*
 * Copyright (c) 2008, Edward Yakop. All Rights Reserved.
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
package org.qi4j.entity;

/**
 * Annotation to denote the unit of work discard policy.
 * By default, discard is applied on any method that has {@link UnitOfWorkPropagation} and any exception is thrown.
 * <p/>
 * Apply {@code UnitOfWorkDiscardOn} to override the default settings.
 *
 * <p/>
 * Usage example:
 * <pre>
 * <code>
 *
 * &#64;Concerns( UnitOfWorkConcern.class )
 * public class MyBusinessServiceMixin implements BusinessService
 * {
 *   &#64;Structure UnitOfWorkFactory uowf;
 *
 *   &#64;UnitOfWorkDiscardOn( MyBusinessException.class )
 *   public void myBusinessMethod()
 *   {
 *     // Must invoke current unit of work.
 *     UnitOfWork uow = uowf.currentUnitOfWork();
 *
 *     // Perform business logic
 *   }
 * }
 * </code>
 * </pre>
 *
 * <p/>
 * The unit of work will be discarded iff {@code MyBusinessException} exceptions or its subclass is thrown from within
 * {@code myBusinessMethod} method.
 *
 * @author edward.yakop@gmail.com
 * @since 0.2.0
 */
public @interface UnitOfWorkDiscardOn
{
    Class<? extends Throwable>[] value() default { Throwable.class };
}
