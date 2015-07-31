/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.runtime.structure;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.functional.Specification;

/**
 * TODO
 */
public class VisibilitySpecification
    implements Specification<ModelDescriptor>
{
    public static final Specification<ModelDescriptor> MODULE = new VisibilitySpecification( Visibility.module );
    public static final Specification<ModelDescriptor> LAYER = new VisibilitySpecification( Visibility.layer );
    public static final Specification<ModelDescriptor> APPLICATION = new VisibilitySpecification( Visibility.application );

    private final Visibility visibility;

    public VisibilitySpecification( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public boolean satisfiedBy( ModelDescriptor item )
    {
        return item.visibility().ordinal() >= visibility.ordinal();
    }
}
