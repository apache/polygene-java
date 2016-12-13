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
package org.apache.polygene.test.cache;

import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceActivation;
import org.apache.polygene.spi.cache.CachePool;

/**
 * In-Memory CachePool Service.
 */
@Mixins( MemoryCachePoolMixin.class )
public interface MemoryCachePoolService
    extends CachePool, ServiceActivation
{
    /**
     * Get the single Cache of this CachePool.
     *
     * @return The single Cache of this CachePool
     * @throws IllegalArgumentException if no or more than one Cache is present in the CachePool
     */
    MemoryCacheImpl<?> singleCache();
}
