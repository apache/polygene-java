/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.io;

/**
 * Input source of data.
 * <p/>
 * Invoke transferTo to send data from this input to given output. transferTo can be invoked
 * as many times as you want. The transferTo implementation must ensure that any exceptions thrown
 * by the Input or the Output which transferred data is sent to is handled properly, i.e. that resources
 * are closed. Any client code to transferTo calls should not have to bother with resource management,
 * but may catch exceptions anyway for logging and similar purposes.
 */
// START SNIPPET: input
public interface Input<T, SenderThrowableType extends Throwable>
{
    <ReceiverThrowableType extends Throwable> void transferTo( Output<? super T, ReceiverThrowableType> output )
        throws SenderThrowableType, ReceiverThrowableType;
}
// END SNIPPET: input
