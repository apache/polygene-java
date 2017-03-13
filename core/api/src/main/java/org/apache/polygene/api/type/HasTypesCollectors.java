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
package org.apache.polygene.api.type;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collectors for HasTypes.
 */
public class HasTypesCollectors
{
    private static final String EQUAL_KEY = "equal";
    private static final String EQUAL_TYPE_KEY = "equalType";
    private static final String ASSIGNABLE_TYPE_KEY = "assignableType";

    public static <T extends HasTypes> Collector<T, ?, Optional<T>> matchingType( T hasTypes )
    {
        return hasTypesFindFirstCollector( hasTypes, new HasAssignableFromType<>( hasTypes ) );
    }

    public static <T extends HasTypes> Collector<T, ?, Optional<T>> closestType( T hasTypes )
    {
        return hasTypesFindFirstCollector( hasTypes, new HasAssignableToType<>( hasTypes ) );
    }

    private static <T extends HasTypes> Collector<T, ?, Optional<T>>
    hasTypesFindFirstCollector( T hasTypes, Predicate<T> assignableTypePredicate )
    {
        Predicate<T> equalPredicate = o -> Objects.equals( o, hasTypes );
        Predicate<T> equalTypePredicate = new HasEqualType<>( hasTypes );
        return new Collector<T, Map<String, Set<T>>, Optional<T>>()
        {
            @Override
            public Supplier<Map<String, Set<T>>> supplier()
            {
                return () -> new HashMap<String, Set<T>>( 3 )
                {{
                    put( EQUAL_KEY, new LinkedHashSet<>( 1 ) );
                    put( EQUAL_TYPE_KEY, new LinkedHashSet<>( 1 ) );
                    put( ASSIGNABLE_TYPE_KEY, new LinkedHashSet<>() );
                }};
            }

            @Override
            public BiConsumer<Map<String, Set<T>>, T> accumulator()
            {
                return ( map, candidate ) ->
                {
                    Set<T> equalObjects = map.get( EQUAL_KEY );
                    if( equalObjects.isEmpty() )
                    {
                        if( equalPredicate.test( candidate ) )
                        {
                            equalObjects.add( candidate );
                        }
                        else
                        {
                            Set<T> equalTypes = map.get( EQUAL_TYPE_KEY );
                            if( equalTypes.isEmpty() )
                            {
                                if( equalTypePredicate.test( candidate ) )
                                {
                                    equalTypes.add( candidate );
                                }
                                else if( assignableTypePredicate.test( candidate ) )
                                {
                                    map.get( ASSIGNABLE_TYPE_KEY ).add( candidate );
                                }
                            }
                        }
                    }
                };
            }

            @Override
            public BinaryOperator<Map<String, Set<T>>> combiner()
            {
                return ( left, right ) ->
                {
                    left.get( EQUAL_KEY ).addAll( right.get( EQUAL_KEY ) );
                    left.get( EQUAL_TYPE_KEY ).addAll( right.get( EQUAL_TYPE_KEY ) );
                    left.get( ASSIGNABLE_TYPE_KEY ).addAll( right.get( ASSIGNABLE_TYPE_KEY ) );
                    return left;
                };
            }

            @Override
            public Function<Map<String, Set<T>>, Optional<T>> finisher()
            {
                return map ->
                {
                    Set<T> equalObjects = map.get( EQUAL_KEY );
                    if( !equalObjects.isEmpty() )
                    {
                        return Optional.of( equalObjects.iterator().next() );
                    }
                    Set<T> equalTypes = map.get( EQUAL_TYPE_KEY );
                    if( !equalTypes.isEmpty() )
                    {
                        return Optional.of( equalTypes.iterator().next() );
                    }
                    Set<T> assignableTypes = map.get( ASSIGNABLE_TYPE_KEY );
                    if( !assignableTypes.isEmpty() )
                    {
                        return Optional.of( assignableTypes.iterator().next() );
                    }
                    return Optional.empty();
                };
            }

            @Override
            public Set<Characteristics> characteristics()
            {
                return Collections.emptySet();
            }
        };
    }


    public static <T extends HasTypes> Collector<T, ?, List<T>> matchingTypes( T hasTypes )
    {
        return hasTypesToListCollector( hasTypes, new HasAssignableFromType<>( hasTypes ) );
    }

    public static <T extends HasTypes> Collector<T, ?, List<T>> closestTypes( T hasTypes )
    {
        return hasTypesToListCollector( hasTypes, new HasAssignableToType<>( hasTypes ) );
    }

    private static <T extends HasTypes> Collector<T, ?, List<T>>
    hasTypesToListCollector( T hasTypes, Predicate<T> assignableTypePredicate )
    {
        Predicate<T> equalPredicate = o -> Objects.equals( o, hasTypes );
        Predicate<T> equalTypePredicate = new HasEqualType<>( hasTypes );
        return new Collector<T, Map<String, Set<T>>, List<T>>()
        {
            @Override
            public Supplier<Map<String, Set<T>>> supplier()
            {
                return () -> new HashMap<String, Set<T>>( 3 )
                {{
                    put( EQUAL_KEY, new LinkedHashSet<>() );
                    put( EQUAL_TYPE_KEY, new LinkedHashSet<>() );
                    put( ASSIGNABLE_TYPE_KEY, new LinkedHashSet<>() );
                }};
            }

            @Override
            public BiConsumer<Map<String, Set<T>>, T> accumulator()
            {
                return ( map, candidate ) ->
                {
                    Set<T> equalObjects = map.get( EQUAL_KEY );
                    if( equalObjects.isEmpty() )
                    {
                        if( equalPredicate.test( candidate ) )
                        {
                            equalObjects.add( candidate );
                        }
                        else
                        {
                            Set<T> equalTypes = map.get( EQUAL_TYPE_KEY );
                            if( equalTypes.isEmpty() )
                            {
                                if( equalTypePredicate.test( candidate ) )
                                {
                                    equalTypes.add( candidate );
                                }
                                else if( assignableTypePredicate.test( candidate ) )
                                {
                                    map.get( ASSIGNABLE_TYPE_KEY ).add( candidate );
                                }
                            }
                        }
                    }
                };
            }

            @Override
            public BinaryOperator<Map<String, Set<T>>> combiner()
            {
                return ( left, right ) ->
                {
                    left.get( EQUAL_KEY ).addAll( right.get( EQUAL_KEY ) );
                    left.get( EQUAL_TYPE_KEY ).addAll( right.get( EQUAL_TYPE_KEY ) );
                    left.get( ASSIGNABLE_TYPE_KEY ).addAll( right.get( ASSIGNABLE_TYPE_KEY ) );
                    return left;
                };
            }

            @Override
            public Function<Map<String, Set<T>>, List<T>> finisher()
            {
                return map ->
                {
                    Set<T> equalObjects = map.get( EQUAL_KEY );
                    Set<T> equalSet = map.get( EQUAL_TYPE_KEY );
                    Set<T> assignableSet = map.get( ASSIGNABLE_TYPE_KEY );
                    List<T> list = new ArrayList<>( equalObjects.size() + equalSet.size() + assignableSet.size() );
                    list.addAll( equalObjects );
                    list.addAll( equalSet );
                    list.addAll( assignableSet );
                    return list;
                };
            }

            @Override
            public Set<Characteristics> characteristics()
            {
                return Collections.emptySet();
            }
        };
    }


    /**
     * Collect a single matching HasTypes.
     *
     * TODO Detail
     *
     * @param type type to match
     * @param <T> type of HasTypes
     * @return an optional best matching HasTypes
     */
    public static <T extends HasTypes> Collector<T, ?, Optional<T>> matchingType( Type type )
    {
        return typeFindFirstCollector( type, new HasAssignableFromType<>( type ) );
    }

    public static <T extends HasTypes> Collector<T, ?, Optional<T>> closestType( Type type )
    {
        return typeFindFirstCollector( type, new HasAssignableToType<T>( type ) );
    }

    private static <T extends HasTypes> Collector<T, ?, Optional<T>>
    typeFindFirstCollector( Type type, Predicate<T> assignableTypePredicate )
    {
        Predicate<T> equalTypePredicate = new HasEqualType<>( type );
        return new Collector<T, Map<String, Set<T>>, Optional<T>>()
        {
            @Override
            public Supplier<Map<String, Set<T>>> supplier()
            {
                return () -> new HashMap<String, Set<T>>( 2 )
                {{
                    put( EQUAL_TYPE_KEY, new LinkedHashSet<>( 1 ) );
                    put( ASSIGNABLE_TYPE_KEY, new LinkedHashSet<>() );
                }};
            }

            @Override
            public BiConsumer<Map<String, Set<T>>, T> accumulator()
            {
                return ( map, candidate ) ->
                {
                    Set<T> equalSet = map.get( EQUAL_TYPE_KEY );
                    if( equalSet.isEmpty() )
                    {
                        if( equalTypePredicate.test( candidate ) )
                        {
                            equalSet.add( candidate );
                        }
                        else if( assignableTypePredicate.test( candidate ) )
                        {
                            map.get( ASSIGNABLE_TYPE_KEY ).add( candidate );
                        }
                    }
                };
            }

            @Override
            public BinaryOperator<Map<String, Set<T>>> combiner()
            {
                return ( left, right ) ->
                {
                    left.get( EQUAL_TYPE_KEY ).addAll( right.get( EQUAL_TYPE_KEY ) );
                    left.get( ASSIGNABLE_TYPE_KEY ).addAll( right.get( ASSIGNABLE_TYPE_KEY ) );
                    return left;
                };
            }

            @Override
            public Function<Map<String, Set<T>>, Optional<T>> finisher()
            {
                return map ->
                {
                    Set<T> equalSet = map.get( EQUAL_TYPE_KEY );
                    if( !equalSet.isEmpty() )
                    {
                        return Optional.of( equalSet.iterator().next() );
                    }
                    Set<T> assignableSet = map.get( ASSIGNABLE_TYPE_KEY );
                    if( !assignableSet.isEmpty() )
                    {
                        return Optional.of( assignableSet.iterator().next() );
                    }
                    return Optional.empty();
                };
            }

            @Override
            public final Set<Characteristics> characteristics()
            {
                return Collections.emptySet();
            }
        };
    }

    /**
     * Collect all matching HasTypes.
     *
     * First the ones with at least on equal type.
     * Then the ones with at least one type assignable from {@literal type}.
     *
     * @param type type to match
     * @param <T> type of HasTypes
     * @return an optional best matching HasTypes
     */
    public static <T extends HasTypes> Collector<T, ?, List<T>> matchingTypes( Type type )
    {
        return typeToListCollector( type, new HasAssignableFromType<>( type ) );
    }

    public static <T extends HasTypes> Collector<T, ?, List<T>> closestTypes( Type type )
    {
        return typeToListCollector( type, new HasAssignableToType<>( type ) );
    }

    private static <T extends HasTypes> Collector<T, ?, List<T>>
    typeToListCollector( Type type, Predicate<T> assignableTypePredicate )
    {
        Predicate<T> equalTypePredicate = new HasEqualType<>( type );
        return new Collector<T, Map<String, Set<T>>, List<T>>()
        {
            @Override
            public Supplier<Map<String, Set<T>>> supplier()
            {
                return () -> new HashMap<String, Set<T>>( 2 )
                {{
                    put( EQUAL_TYPE_KEY, new LinkedHashSet<>() );
                    put( ASSIGNABLE_TYPE_KEY, new LinkedHashSet<>() );
                }};
            }

            @Override
            public BiConsumer<Map<String, Set<T>>, T> accumulator()
            {
                return ( map, candidate ) ->
                {
                    if( equalTypePredicate.test( candidate ) )
                    {
                        map.get( EQUAL_TYPE_KEY ).add( candidate );
                    }
                    else if( assignableTypePredicate.test( candidate ) )
                    {
                        map.get( ASSIGNABLE_TYPE_KEY ).add( candidate );
                    }
                };
            }

            @Override
            public BinaryOperator<Map<String, Set<T>>> combiner()
            {
                return ( left, right ) ->
                {
                    left.get( EQUAL_TYPE_KEY ).addAll( right.get( EQUAL_TYPE_KEY ) );
                    left.get( ASSIGNABLE_TYPE_KEY ).addAll( right.get( ASSIGNABLE_TYPE_KEY ) );
                    return left;
                };
            }

            @Override
            public Function<Map<String, Set<T>>, List<T>> finisher()
            {
                return map ->
                {
                    Set<T> equalSet = map.get( EQUAL_TYPE_KEY );
                    Set<T> assignableSet = map.get( ASSIGNABLE_TYPE_KEY );
                    List<T> list = new ArrayList<>( equalSet.size() + assignableSet.size() );
                    list.addAll( equalSet );
                    list.addAll( assignableSet );
                    return list;
                };
            }

            @Override
            public final Set<Characteristics> characteristics()
            {
                return Collections.emptySet();
            }
        };
    }

    private HasTypesCollectors() {}
}
