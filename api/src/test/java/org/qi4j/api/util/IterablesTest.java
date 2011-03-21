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

package org.qi4j.api.util;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.specification.Specifications;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test of Iterables utility methods
 */
public class IterablesTest
{
   private List<String> numbers = Arrays.asList("1", "2", "3");
   private Iterable<? extends Number> numberLongs = Arrays.asList(1L, 2L, 3L);
   private Iterable<? extends Number> numberIntegers = Arrays.asList(1, 2, 3);

   @Test
   public void testAddAll()
   {
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(), numbers).toString(), equalTo("[1, 2, 3]"));
      Assert.assertThat(Iterables.addAll(new ArrayList<Number>(), numberLongs).toString(), equalTo("[1, 2, 3]"));
   }

   @Test
   public void testCount()
   {
      Assert.assertThat(Iterables.count(numbers), equalTo(3L));
   }

   @Test
   public void testFilter()
   {
      Assert.assertThat(Iterables.first(Iterables.filter(Specifications.in("2"), numbers)), equalTo("2"));
   }

   @Test
   public void testFirst()
   {
      Assert.assertThat(Iterables.first(numbers), equalTo("1"));
      Assert.assertThat(Iterables.first(Collections.<Object>emptyList()), CoreMatchers.<Object>nullValue());
   }

   @Test
   public void testMatchesAny()
   {
      Assert.assertThat(Iterables.matchesAny(Specifications.in("2"), numbers), equalTo(true));
      Assert.assertThat(Iterables.matchesAny(Specifications.in("4"), numbers), equalTo(false));
   }

   @Test
   public void testFlatten()
   {
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(), Iterables.flatten(numbers, numbers))
              .toString(), equalTo("[1, 2, 3, 1, 2, 3]"));

      Assert.assertThat(Iterables.addAll(new ArrayList<Number>(), Iterables.flatten((Iterable<Number>) numberIntegers, (Iterable<Number>) numberLongs))
              .toString(), equalTo("[1, 2, 3, 1, 2, 3]"));
   }

   @Test
   public void testFlattenIterables()
   {
      Iterable<Iterable<String>> iterable = Iterables.iterable(numbers, (Iterable<String>) numbers);
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(),
              Iterables.flatten(iterable))
              .toString(), equalTo("[1, 2, 3, 1, 2, 3]"));
   }

   @Test
   public void testMap()
   {
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(), Iterables.map(new Function<String, String>()
      {
         public String map(String s)
         {
            return s + s;
         }
      }, numbers)).toString(), equalTo("[11, 22, 33]"));


      Iterable<List<String>> numberIterable = Iterables.iterable(numbers, numbers, numbers);
      Assert.assertThat(Iterables.addAll(new ArrayList<Integer>(), Iterables.map(new Function<Collection, Integer>()
      {
         @Override
         public Integer map(Collection collection)
         {
            return collection.size();
         }
      }, numberIterable)).toString(), equalTo("[3, 3, 3]"));
   }

   @Test
   public void testIterableEnumeration()
   {

      Enumeration<String> enumeration = Collections.enumeration(numbers);
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(), Iterables.iterable(enumeration))
              .toString(), equalTo("[1, 2, 3]"));
   }

   @Test
   public void testIterableVarArg()
   {
      Assert.assertThat(Iterables.addAll(new ArrayList<String>(), Iterables.iterable("1", "2", "3"))
              .toString(), equalTo("[1, 2, 3]"));
   }
}
