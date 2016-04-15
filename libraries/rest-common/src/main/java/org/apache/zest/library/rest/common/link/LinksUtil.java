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

package org.apache.zest.library.rest.common.link;

import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.zest.functional.Iterables;

/**
 * Helper methods for links
 */
public final class LinksUtil
{
   public static Predicate<Link> withId(final String id)
   {
      return link -> link.id().get().equals( id);
   }

   public static Predicate<Link> withText(final String text)
   {
      return link -> link.text().get().equals( text);
   }

   public static Predicate<Link> withRel(final String rel)
   {
      return link -> link.rel().get().equals( rel);
   }

   public static Predicate<Link> withClass(final String clazz)
   {
      return link -> {
          String classes = link.classes().get();
          return classes != null && classes.contains( clazz );
      };
   }

    public static Link withRel(String rel, Links links)
    {
        return Iterables.single( Iterables.filter( withRel( rel ), links.links().get() ) );
    }

    public static Link withId(String id, Links links)
    {
        return Iterables.single( Iterables.filter( withId( id ), links.links().get() ) );
    }

   public static Function<Link, String> toRel()
   {
      return link -> link.rel().get();
   }
}
