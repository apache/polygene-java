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
package org.qi4j.spi.module;

import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.spi.entitystore.EntityStore;

public interface ModuleSpi extends Module
{
    EntityStore entityStore();

    IdentityGenerator identityGenerator();

    ValueSerialization valueSerialization();

    Iterable<ModelModule<EntityDescriptor>> findVisibleEntityTypes();

    Iterable<ModelModule<ValueDescriptor>> findVisibleValueTypes();

    Iterable<ModelModule<TransientDescriptor>> findVisibleTransientTypes();

    Iterable<ModelModule<ObjectDescriptor>> findVisibleObjectTypes();

    Iterable<ModelModule<ServiceDescriptor>> findVisibleServiceTypes();
}
