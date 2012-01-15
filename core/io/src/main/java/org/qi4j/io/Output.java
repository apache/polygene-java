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
 * Output for data.
 */
// START SNIPPET: output
public interface Output<T, ReceiverThrowableType extends Throwable>
{
// END SNIPPET: output
    /**
     * This initiates a transfer from an Input. Implementations should open any resources to be written to
     * and then call sender.sendTo() when it is ready to receive data. When sendTo() returns the resource should be
     * closed properly. Make sure to handle any exceptions from sendTo.
     *
     * @param sender                the sender of data to this output
     * @param <SenderThrowableType> the exception that sendTo can throw
     *
     * @throws SenderThrowableType   the exception that the sender can throw
     * @throws ReceiverThrowableType the exception that this output can throw from receiveItem()
     */
// START SNIPPET: output
    <SenderThrowableType extends Throwable> void receiveFrom( Sender<? extends T, SenderThrowableType> sender )
        throws ReceiverThrowableType, SenderThrowableType;
}
// END SNIPPET: output
