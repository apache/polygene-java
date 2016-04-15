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
package org.apache.zest.sample.dcicargo.sample_b.data.structure.location;

import org.apache.zest.api.property.Property;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.library.constraints.annotation.Matches;

/**
 * UnLocode
 *
 * A United Nations location code.
 *
 * http://www.unece.org/cefact/locode/
 * http://www.unece.org/cefact/locode/DocColumnDescription.htm#LOCODE
 *
 * UnLocode is mandatory and immutable.
 */
public interface UnLocode
    extends ValueComposite
{
    // Country code is exactly two letters.
    // Location code is usually three letters, but may contain the numbers 2-9 as well
    @Matches( "[a-zA-Z]{2}[a-zA-Z2-9]{3}" )
    Property<String> code();
}
