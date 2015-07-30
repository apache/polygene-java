/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.apache.zest.test.mock;

import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.test.mock.internal.MockRecorderMixin;

/**
 * A mocked composite. Adds {@link org.apache.zest.test.mock.MockRecorder} to the mocked composite.
 */
@Mixins( { MockRecorderMixin.class } )
public interface MockComposite
    extends MockRecorder, TransientComposite
{
}