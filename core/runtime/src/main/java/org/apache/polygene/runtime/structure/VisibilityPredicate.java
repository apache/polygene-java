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
package org.apache.polygene.runtime.structure;

import java.util.function.Predicate;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.composite.ModelDescriptor;

/**
 * TODO
 */
public class VisibilityPredicate
    implements Predicate<ModelDescriptor>
{
    public static final Predicate<ModelDescriptor> MODULE = new VisibilityPredicate( Visibility.module );
    public static final Predicate<ModelDescriptor> LAYER = new VisibilityPredicate( Visibility.layer );
    public static final Predicate<ModelDescriptor> APPLICATION = new VisibilityPredicate( Visibility.application );

    private final Visibility visibility;

    public VisibilityPredicate( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public boolean test( ModelDescriptor item )
    {
        return item.visibility().ordinal() >= visibility.ordinal();
    }
}
