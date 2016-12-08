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
package org.apache.zest.library.rest.server.restlet.freemarker;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDescriptor;

/**
 * TODO
 */
public class ValueCompositeTemplateModel
    implements TemplateHashModelEx, TemplateScalarModel
{
    private ValueComposite composite;
    private ObjectWrapper wrapper;
    private ValueDescriptor descriptor;

    public ValueCompositeTemplateModel( ValueComposite composite, ObjectWrapper wrapper )
    {
        this.composite = composite;
        this.wrapper = wrapper;
        descriptor = (ValueDescriptor) ZestAPI.FUNCTION_DESCRIPTOR_FOR.apply( composite );
    }

    @Override
    public int size()
        throws TemplateModelException
    {
        return (int) descriptor.state().properties().count();
    }

    @Override
    public TemplateCollectionModel keys()
        throws TemplateModelException
    {
        List<String> names = descriptor.state().properties()
            .map( descriptor -> descriptor.qualifiedName().name() )
            .collect( Collectors.toList() );
        return (TemplateCollectionModel) wrapper.wrap( names.iterator() );
    }

    @Override
    public TemplateCollectionModel values()
        throws TemplateModelException
    {
        List<Object> values = ZestAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( composite )
            .state().properties()
            .map( (Function<Property<?>, Object>) objectProperty -> {
                try
                {
                    return wrapper.wrap( objectProperty.get() );
                }
                catch( TemplateModelException e )
                {
                    throw new IllegalStateException( e );
                }
            } )
            .collect( Collectors.toList() );

        return (TemplateCollectionModel) wrapper.wrap( values );
    }

    @Override
    public TemplateModel get( String key )
        throws TemplateModelException
    {
        try
        {
            return wrapper.wrap( ZestAPI.FUNCTION_COMPOSITE_INSTANCE_OF
                                     .apply( composite )
                                     .state()
                                     .propertyFor( descriptor.state().findPropertyModelByName( key ).accessor() )
                                     .get() );
        }
        catch( IllegalArgumentException e )
        {
            return null;
        }
    }

    @Override
    public boolean isEmpty()
        throws TemplateModelException
    {
        return size() == 0;
    }

    @Override
    public String getAsString()
        throws TemplateModelException
    {
        return composite.toString();
    }
}
