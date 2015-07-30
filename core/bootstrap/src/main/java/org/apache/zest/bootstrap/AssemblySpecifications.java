/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.bootstrap;

import org.apache.zest.api.type.HasTypes;
import org.apache.zest.functional.Specification;
import org.apache.zest.functional.Specifications;

/**
 * Utility specifications for Assemblies.
 */
public class AssemblySpecifications
{
    public static Specification<HasTypes> types( final Class... types )
    {
        return new Specification<HasTypes>()
        {
            @Override
            public boolean satisfiedBy( HasTypes item )
            {

                for( Class<?> type : item.types() )
                {
                    if( Specifications.in( types ).satisfiedBy( type ) )
                    {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
