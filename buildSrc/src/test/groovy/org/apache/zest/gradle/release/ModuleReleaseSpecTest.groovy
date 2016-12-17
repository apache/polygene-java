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
 */
package org.apache.zest.gradle.release

import spock.lang.Specification
import spock.lang.Unroll

class ModuleReleaseSpecTest extends Specification
{
  @Unroll
  def "codebase(#code) docs(#docs) tests(#tests) -> #expected"()
  {
    expect:
    new ModuleReleaseSpec().satisfiedBy( code, docs, tests ) == expected

    where:
    code     | docs       | tests      | expected
    'none'   | 'none'     | 'none'     | false
    'none'   | 'none'     | 'some'     | false
    'none'   | 'none'     | 'good'     | false
    'none'   | 'none'     | 'complete' | false
    'none'   | 'brief'    | 'none'     | false
    'none'   | 'brief'    | 'some'     | false
    'none'   | 'brief'    | 'good'     | false
    'none'   | 'brief'    | 'complete' | false
    'none'   | 'good'     | 'none'     | false
    'none'   | 'good'     | 'some'     | false
    'none'   | 'good'     | 'good'     | false
    'none'   | 'good'     | 'complete' | false
    'none'   | 'complete' | 'none'     | true
    'none'   | 'complete' | 'some'     | true
    'none'   | 'complete' | 'good'     | true
    'none'   | 'complete' | 'complete' | true

    'early'  | 'none'     | 'none'     | false
    'early'  | 'none'     | 'some'     | false
    'early'  | 'none'     | 'good'     | false
    'early'  | 'none'     | 'complete' | false
    'early'  | 'brief'    | 'none'     | false
    'early'  | 'brief'    | 'some'     | false
    'early'  | 'brief'    | 'good'     | false
    'early'  | 'brief'    | 'complete' | false
    'early'  | 'good'     | 'none'     | false
    'early'  | 'good'     | 'some'     | false
    'early'  | 'good'     | 'good'     | true
    'early'  | 'good'     | 'complete' | true
    'early'  | 'complete' | 'none'     | false
    'early'  | 'complete' | 'some'     | false
    'early'  | 'complete' | 'good'     | true
    'early'  | 'complete' | 'complete' | true

    'beta'   | 'none'     | 'none'     | false
    'beta'   | 'none'     | 'some'     | false
    'beta'   | 'none'     | 'good'     | false
    'beta'   | 'none'     | 'complete' | false
    'beta'   | 'brief'    | 'none'     | false
    'beta'   | 'brief'    | 'some'     | true
    'beta'   | 'brief'    | 'good'     | true
    'beta'   | 'brief'    | 'complete' | true
    'beta'   | 'good'     | 'none'     | false
    'beta'   | 'good'     | 'some'     | true
    'beta'   | 'good'     | 'good'     | true
    'beta'   | 'good'     | 'complete' | true
    'beta'   | 'complete' | 'none'     | false
    'beta'   | 'complete' | 'some'     | true
    'beta'   | 'complete' | 'good'     | true
    'beta'   | 'complete' | 'complete' | true

    'stable' | 'none'     | 'none'     | true
    'stable' | 'none'     | 'some'     | true
    'stable' | 'none'     | 'good'     | true
    'stable' | 'none'     | 'complete' | true
    'stable' | 'brief'    | 'none'     | true
    'stable' | 'brief'    | 'some'     | true
    'stable' | 'brief'    | 'good'     | true
    'stable' | 'brief'    | 'complete' | true
    'stable' | 'good'     | 'none'     | true
    'stable' | 'good'     | 'some'     | true
    'stable' | 'good'     | 'good'     | true
    'stable' | 'good'     | 'complete' | true
    'stable' | 'complete' | 'none'     | true
    'stable' | 'complete' | 'some'     | true
    'stable' | 'complete' | 'good'     | true
    'stable' | 'complete' | 'complete' | true

    'mature' | 'none'     | 'none'     | true
    'mature' | 'none'     | 'some'     | true
    'mature' | 'none'     | 'good'     | true
    'mature' | 'none'     | 'complete' | true
    'mature' | 'brief'    | 'none'     | true
    'mature' | 'brief'    | 'some'     | true
    'mature' | 'brief'    | 'good'     | true
    'mature' | 'brief'    | 'complete' | true
    'mature' | 'good'     | 'none'     | true
    'mature' | 'good'     | 'some'     | true
    'mature' | 'good'     | 'good'     | true
    'mature' | 'good'     | 'complete' | true
    'mature' | 'complete' | 'none'     | true
    'mature' | 'complete' | 'some'     | true
    'mature' | 'complete' | 'good'     | true
    'mature' | 'complete' | 'complete' | true
  }
}
