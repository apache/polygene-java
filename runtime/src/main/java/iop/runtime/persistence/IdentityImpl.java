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
package iop.runtime.persistence;

import iop.api.persistence.Identity;
import iop.api.persistence.modifier.IdentityValidatorModifier;
import iop.api.annotation.ModifiedBy;

/**
 * This mixin contains the identity of an object.
 *
 */
@ModifiedBy(IdentityValidatorModifier.class)
public final class IdentityImpl
   implements Identity
{
   String identity;

   public void setIdentity(String anIdentity)
   {
      identity = anIdentity;
   }

   public String getIdentity()
   {
      return identity;
   }
}
