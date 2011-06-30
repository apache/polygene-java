/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.samples.dddsample.application.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This interface is used to mark methods as being nested UnitOfWork's.
 * A nested UnitOfWork will be created upon invocation of the method.
 * If no UnitOfWork is in progress, a new root UoW will be created.
 * When the method returns the nested UoW will be completed.
 * <p/>
 * The invoked method is allowed to pause() the nested UoW, in which case
 * it will not be completed upon return from the method. The client is
 * expected to either throw away the results of the method (thus GC'ing
 * the associated UoW), or resume() the UoW and then complete() it manually.
 */
@Retention( RUNTIME )
@Target( { TYPE, METHOD } )
public @interface NestedUnitOfWork
{
}
