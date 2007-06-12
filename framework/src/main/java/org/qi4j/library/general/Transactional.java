/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.library.general;

/**
 * Example transaction attribute.
 *
 * Note: preferably one should use the Spring tx annotations instead!
 * This is just as a sample of how tx support could work.
 *
 */
public @interface Transactional
{
    public static final int NEVER = 0;
    public static final int NOT_SUPPORTED = 1;
    public static final int REQUIRED = 2;
    public static final int REQUIRES_NEW = 3;
    public static final int SUPPORTS = 4;

    int value();
}
