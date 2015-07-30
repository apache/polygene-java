/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.apache.zest.library.logging.trace.service;

import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.sideeffect.SideEffects;
import org.apache.zest.library.logging.trace.TraceOnConsoleSideEffect;

@SideEffects( TraceOnConsoleSideEffect.class )
@Mixins( TraceServiceMixin.class )
public interface StandardTraceServiceComposite extends TraceService, ServiceComposite
{
}
