/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.eventsourcing.domain.factory;

import org.qi4j.library.eventsourcing.domain.spi.CurrentUser;

import javax.security.auth.Subject;
import java.security.AccessController;

/**
 * Uses thread-associated Subject to get the current user name. Default to "unknown".
 */
public class CurrentUserSubject
    implements CurrentUser
{
    public String getCurrentUser()
    {
        try
        {
            return Subject.getSubject( AccessController.getContext() ).getPrincipals().iterator().next().getName();
        } catch (Exception e)
        {
            return "unknown";
        }
    }
}
