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

package org.apache.zest.runtime.value;

import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.util.HierarchicalVisitor;
import org.apache.zest.api.util.VisitableHierarchy;
import org.apache.zest.api.value.ValueDescriptor;

/**
 * JAVADOC
 */
public final class ValuesModel
    implements VisitableHierarchy<Object, Object>
{
    private final List<ValueModel> valueModels;

    public ValuesModel( List<ValueModel> valueModels )
    {
        this.valueModels = valueModels;
    }

    public Stream<ValueModel> models()
    {
        return valueModels.stream();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ValueModel valueModel : valueModels )
            {
                if( !valueModel.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public Stream<? extends ValueDescriptor> stream()
    {
        return valueModels.stream();
    }
}