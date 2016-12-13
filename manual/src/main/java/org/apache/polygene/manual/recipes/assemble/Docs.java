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
package org.apache.polygene.manual.recipes.assemble;

import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueComposite;

public class Docs
{
    @This
    UnitOfWorkFactory module;

    public void showUsage()
    {
        {
// START SNIPPET: indirect
            UnitOfWork unitOfWork = module.currentUnitOfWork();
            Person person = unitOfWork.newEntity( Person.class );
// END SNIPPET: indirect
        }
        {
// START SNIPPET: direct
            UnitOfWork unitOfWork = module.currentUnitOfWork();
            PersonEntity person = unitOfWork.newEntity( PersonEntity.class );
// END SNIPPET: direct
        }
    }

    public interface Person
    {
    }

    public interface PersonEntity extends ValueComposite
    {
    }
}
