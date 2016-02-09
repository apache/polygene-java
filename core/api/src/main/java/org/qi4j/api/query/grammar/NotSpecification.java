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
package org.qi4j.api.query.grammar;

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;

/**
 * NOT Specification.
 */
public class NotSpecification implements Specification<Composite>
{
    private Specification<Composite> operand;

    public NotSpecification( Specification<Composite> operand )
    {
        this.operand = operand;
    }

    public Specification<Composite> operand()
    {
        return operand;
    }

    @Override
    public boolean satisfiedBy( Composite item )
    {
        return Specifications.not( operand ).satisfiedBy( item );
    }

    @Override
    public String toString()
    {
        return "!" + operand.toString();
    }
}