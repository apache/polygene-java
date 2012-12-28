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

package org.qi4j.library.rest.common;

import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.Iterables;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.common.link.LinksUtil;

/**
 * Value representing a whole resource in a URL path. Allows listing of available
 * queries, commands, sub-resources and an index.
 */
@Mixins(Resource.Mixin.class)
public interface Resource
    extends ValueComposite
{
    @UseDefaults
    Property<List<Link>> queries();

    Link query(String relation);

    @UseDefaults
    Property<List<Link>> commands();

    Link command(String relation);

    @UseDefaults
    Property<List<Link>> resources();

    Link resource(String relation);

    @Optional
    Property<ValueComposite> index();

    abstract class Mixin
        implements Resource
    {
        @Override
        public Link query( String relation )
        {
            return Iterables.single( Iterables.filter( LinksUtil.withRel( relation ), queries().get() ) );
        }

        @Override
        public Link command( String relation )
        {
            return Iterables.single(Iterables.filter( LinksUtil.withRel( relation ), commands().get() ));
        }

        @Override
        public Link resource( String relation )
        {
            return Iterables.single(Iterables.filter( LinksUtil.withRel( relation ), resources().get() ));
        }
    }
}
