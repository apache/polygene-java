/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.common.table;

import java.util.List;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * A table of rows. RowValue needs to be subtyped in order to add columns.
 */
@Mixins(Table.Mixin.class)
public interface Table
      extends ValueComposite
{
   public static final String STRING = "string";
   public static final String NUMBER = "number";
   public static final String BOOLEAN = "boolean";
   public static final String DATE = "date";
   public static final String DATETIME = "datetime";
   public static final String TIME_OF_DAY = "timeofday";

   @UseDefaults
   Property<List<Column>> cols();

   @UseDefaults
   Property<List<Row>> rows();

   Cell cell(Row row, String name);

   abstract class Mixin
      implements Table
   {
      @State
      Property<List<Column>> cols;

      @Override
      public Cell cell(Row row, String columnName)
      {
         for (int i = 0; i < cols.get().size(); i++)
         {
            Column column = cols.get().get(i);
            if ( column.id().get().equals(columnName))
               return row.c().get().get(i);
         }

         return null;
      }
   }
}