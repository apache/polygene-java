/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.zest.spi.module;

import java.util.stream.Stream;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.IdentityGenerator;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.spi.entitystore.EntityStore;

public interface ModuleSpi extends Module
{
    EntityStore entityStore();

    IdentityGenerator identityGenerator();

    ValueSerialization valueSerialization();

    Stream<ModelModule<? extends ModelDescriptor>> findVisibleEntityTypes();

    Stream<ModelModule<? extends ModelDescriptor>> findVisibleValueTypes();

    Stream<ModelModule<? extends ModelDescriptor>> findVisibleTransientTypes();

    Stream<ModelModule<? extends ModelDescriptor>> findVisibleObjectTypes();

    Stream<ServiceReference<?>> findVisibleServiceTypes();
}
