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
package org.apache.zest.sample.dcicargo.sample_b.data.aggregateroot;

import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.sample.dcicargo.sample_b.data.factory.CargoFactory;

/**
 * Cargo aggregate root
 *
 * An identified unique starting point to create Cargos.
 *
 * Cargos are only allowed to be created through this aggregate root. Can we enforce this?
 */
public interface CargoAggregateRoot
    extends EntityComposite,

            CargoFactory
{
    public static final String CARGOS_ID = "Cargos_id";
}
