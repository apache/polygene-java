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

package org.apache.zest.library.eventsourcing.domain.factory;

import java.security.Principal;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.library.eventsourcing.domain.spi.CurrentUser;

/**
 * CurrentUser implementation that gets a Principal object from the meta-info of the current UnitOfWork. Default to "unknown".
 */
public class CurrentUserUoWPrincipal
    implements CurrentUser
{
    @Structure
    private UnitOfWorkFactory uowf;

    @Override
    public String getCurrentUser()
    {
        try
        {
            return uowf.currentUnitOfWork().metaInfo( Principal.class ).getName();
        } catch (Exception e)
        {
            return "unknown";
        }
    }
}
