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
 * Sender of items for a particular transfer from an Input to an Output
 */
// START SNIPPET: sender
public interface Sender<T, SenderThrowableType extends Throwable>
{
// END SNIPPET: sender
    /**
     * The sender should send all items it holds to the receiver by invoking receiveItem for each item.
     *
     * If the receive fails it should properly close any open resources.
     *
     * @param receiver
     * @param <ReceiverThrowableType>
     *
     * @throws ReceiverThrowableType
     * @throws SenderThrowableType
     */
// START SNIPPET: sender
    <ReceiverThrowableType extends Throwable> void sendTo( Receiver<? super T, ReceiverThrowableType> receiver )
        throws ReceiverThrowableType, SenderThrowableType;
}
// END SNIPPET: sender
