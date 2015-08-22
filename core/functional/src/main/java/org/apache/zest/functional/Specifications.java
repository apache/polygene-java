/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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
package org.apache.zest.functional;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Common generic specification expressions
 */
public class Specifications
{
    public static <T> Predicate<T> TRUE()
    {
        return new Predicate<T>()
        {
            @Override
            public boolean test( T instance )
            {
                return true;
            }
        };
    }

    public static <T> Predicate<T> not( final Predicate<T> specification )
    {
        return new Predicate<T>()
        {
            @Override
            public boolean test( T instance )
            {
                return !specification.test( instance );
            }
        };
    }

    @SafeVarargs
    public static <T> AndSpecification<T> and( final Predicate<T>... specifications )
    {
        return and( Iterables.iterable( specifications ) );
    }

    public static <T> AndSpecification<T> and( final Iterable<Predicate<T>> specifications )
    {
        return new AndSpecification<>( specifications );
    }

    @SafeVarargs
    public static <T> OrSpecification<T> or( final Predicate<T>... specifications )
    {
        return or( Iterables.iterable( specifications ) );
    }

    public static <T> OrSpecification<T> or( final Iterable<Predicate<T>> specifications )
    {
        return new OrSpecification<>( specifications );
    }

    @SafeVarargs
    public static <T> Predicate<T> in( final T... allowed )
    {
        return in( Iterables.iterable( allowed ) );
    }

    public static <T> Predicate<T> in( final Iterable<T> allowed )
    {
        return new Predicate<T>()
        {
            @Override
            public boolean test( T item )
            {
                for( T allow : allowed )
                {
                    if( allow.equals( item ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static <T> Predicate<T> notNull()
    {
        return new Predicate<T>()
        {
            @Override
            public boolean test( T item )
            {
                return item != null;
            }
        };
    }

    public static <FROM, TO> Predicate<FROM> translate( final Function<FROM, TO> function,
                                                        final Predicate<? super TO> specification
    )
    {
        return new Predicate<FROM>()
        {
            @Override
            public boolean test( FROM item )
            {
                return specification.test( function.apply( item ) );
            }
        };
    }

    /**
     * AND Specification.
     */
    public static class AndSpecification<T>
        implements Predicate<T>
    {
        private final Iterable<Predicate<T>> specifications;

        private AndSpecification( Iterable<Predicate<T>> specifications )
        {
            this.specifications = specifications;
        }

        @Override
        public boolean test( T instance )
        {
            for( Predicate<T> specification : specifications )
            {
                if( !specification.test( instance ) )
                {
                    return false;
                }
            }

            return true;
        }

        @SafeVarargs
        public final AndSpecification<T> and( Predicate<T>... specifications )
        {
            Iterable<Predicate<T>> iterable = Iterables.iterable( specifications );
            Iterable<Predicate<T>> flatten = Iterables.flatten( this.specifications, iterable );
            return Specifications.and( flatten );
        }

        @SafeVarargs
        public final OrSpecification<T> or( Predicate<T>... specifications )
        {
            return Specifications.or( Iterables.prepend( this, Iterables.iterable( specifications ) ) );
        }
    }

    /**
     * OR Specification.
     */
    public static class OrSpecification<T>
        implements Predicate<T>
    {
        private final Iterable<Predicate<T>> specifications;

        private OrSpecification( Iterable<Predicate<T>> specifications )
        {
            this.specifications = specifications;
        }

        @Override
        public boolean test( T instance )
        {
            for( Predicate<T> specification : specifications )
            {
                if( specification.test( instance ) )
                {
                    return true;
                }
            }

            return false;
        }

        @SafeVarargs
        public final AndSpecification<T> and( Predicate<T>... specifications )
        {
            return Specifications.and( Iterables.prepend( this, Iterables.iterable( specifications ) ) );
        }

        @SafeVarargs
        public final OrSpecification<T> or( Predicate<T>... specifications )
        {
            Iterable<Predicate<T>> iterable = Iterables.iterable( specifications );
            Iterable<Predicate<T>> flatten = Iterables.flatten( this.specifications, iterable );
            return Specifications.or( flatten );
        }
    }

    private Specifications()
    {
    }
}
