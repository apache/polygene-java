/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 * Copyright (c) 2007, Lan Boon Ping. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model;

import org.qi4j.api.annotation.Mixins;
import org.qi4j.library.framework.properties.PropertiesMixin;

/**
 * Generic interface for a contact such as phone number, fax-number, email, msn, etc.
 */
@Mixins( PropertiesMixin.class )
public interface Contact
{
    public final static int CONTACT_VALUE_LEN = 250;

    public final static int CONTACT_TYPE_LEN = 120;

    public void setContactValue( String contactValue );

    public String getContactValue();

    public void setContactType( String contactType );

    public String getContactType();
}
