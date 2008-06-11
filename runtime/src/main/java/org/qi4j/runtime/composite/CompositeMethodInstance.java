/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime.composite;

/**
 * Compaction Level of the StackTrace clenaup operation.
 *
 * <pre>
 * <b>off</b>       = Do not modify the stack trace.
 * <b>proxy</b>     = Remove all Qi4j internal classes and all JDK internal classes from
 *             the originating method call.
 * <b>semi</b>      = Remove all JDK internal classes on the entire stack.
 * <b>extensive</b> = Remove all Qi4j internal and JDK internal classes from the entire stack.
 * </pre>
 *
 * <p>
 * The Compaction is set through the System Property "<code><b>qi4j.compacttrace</b></code>" to
 * any of the above values.
 * </p>
 */
enum CompactLevel
{
    off, proxy, semi, extensive
}