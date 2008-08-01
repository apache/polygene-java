/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.injection.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.qi4j.injection.InjectionScope;

/**
 * Annotation to denote the injection of a Query, QueryBuilder, or Entity prototype dependency into a Fragment.
 * <p/>
 * This annotation is only valid in Fragments in EntityComposites since the queries and builders have to be created within
 * the scope of a particular UnitOfWork, namely the unit of the injected EntityComposite.
 * <p/>
 * Examples:
 * <code><pre>
 * &#64;Entity Query<Person> findPeople; // call findPeople.iterator() to execute query
 * &#64;Entity QueryBuilder<Person> findByName;
 * &#64;Entity Iterable<Person> personPrototype; // call personPrototype.iterator().next() to create new Person
 * </pre></code>
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.PARAMETER } )
@Documented
@InjectionScope
public @interface Entity
{
    String value() default ""; // This name can be used for lookups of named queries
}
