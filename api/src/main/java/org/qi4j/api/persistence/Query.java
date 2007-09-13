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
package org.qi4j.api.persistence;

/**
 * Query of objects from underlying stores.
 * <p/>
 * Example;
 * <code><pre>
 * Query q = em.createQuery(PersonComposite.class);
 * q.where(Name.class).setName("foo");
 * q.where(Age.class, Is.LESS_THAN).setAge(30);
 * q.orderBy(Name.class).getName();
 * List<PersonComposite> result = q.find();
 * </pre></code>
 */
public interface Query<R>
    extends Iterable<R>
{
    void resultType(Class mixinType);

    <K> K where( Class<K> mixinType );

    <K> K where( Class<K> mixinType, Is comparisonOperator );

    <K> K orderBy( Class<K> mixinType );

    <K> K orderBy( Class<K> mixinType, OrderBy order );

    void setFirstResult(int firstResult);

    void setMaxResults(int maxResults);

    Iterable<R> prepare();

    R find();

    public enum Is
    {
        EQUAL, NOT_EQUAL, // Boolean
        LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, // Numerical
        CONTAINS, STARTS_WITH, ENDS_WITH, MATCHES // String
    }

    public enum OrderBy
    {
        ASCENDING, DESCENDING
    }
}
