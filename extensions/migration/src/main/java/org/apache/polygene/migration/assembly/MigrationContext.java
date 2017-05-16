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
package org.apache.polygene.migration.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MigrationContext
{
    private boolean changed;
    private final List<String> failures = new ArrayList<>();

    public boolean hasChanged()
    {
        return changed;
    }

    public void markAsChanged()
    {
        changed = true;
    }

    public boolean isSuccess()
    {
        return failures.isEmpty();
    }

    public boolean hasFailures()
    {
        return failures.size() > 0;
    }

    public List<String> failures()
    {
        return Collections.unmodifiableList( failures );
    }

    public void addFailure( String operation )
    {
        failures.add( operation );
    }
}
