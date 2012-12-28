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
package org.qi4j.library.uid.sequence;

/**
 * Sequencing is used to automatically generate a sequence of numbers.
 * The algorithm is that <code>currentSequenceValue</code> is the number that was last returned
 * in a <code>newSequenceValue</code> call, and will initially be zero. Persisting Sequencing
 * services defines "initially" as the first run ever, as subsequent starts may retrieve the
 * <code>currentSequenceValue</code> from an <code>EntityStore</code>
 */
public interface Sequencing
{
    Long newSequenceValue()
        throws SequencingException;

    Long currentSequenceValue();
}
