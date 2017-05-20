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
package org.apache.polygene.library.constraints.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.apache.polygene.api.constraint.ConstraintDeclaration;
import org.apache.polygene.api.constraint.Constraints;
import org.apache.polygene.library.constraints.HostPortConstraint;

/**
 * Marks a property as being a string, with a "host:port", where host is a valid hostname, IPv4 address or IPv6 address
 * and port is in the range of 0 to 65535
 */
@ConstraintDeclaration
@Retention( RetentionPolicy.RUNTIME )
@Constraints( HostPortConstraint.class )
public @interface HostPort
{
}
