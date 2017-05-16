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

package org.apache.polygene.library.rest.common;

import java.util.List;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.library.rest.common.link.Link;
import org.apache.polygene.library.rest.common.link.LinksUtil;

import static org.apache.polygene.api.util.Collectors.single;

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
    Property<Object> index();

    abstract class Mixin
        implements Resource
    {
        @Override
        public Link query( String relation )
        {
            return queries().get().stream().filter( LinksUtil.withRel( relation ) ).collect( single() );
        }

        @Override
        public Link command( String relation )
        {
            return commands().get().stream().filter( LinksUtil.withRel( relation ) ).collect( single() );
        }

        @Override
        public Link resource( String relation )
        {
            return resources().get().stream().filter( LinksUtil.withRel( relation ) ).collect( single() );
        }
    }
}
