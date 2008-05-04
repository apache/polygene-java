/*
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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

package org.qi4j.entity.quick;

final class RandomAccessRecord
{
    String identity;
    long indexPointer;
    boolean active;
    long dataPointer;
    long valuePointer;
    int dataLength;
    int maxSize;

    public RandomAccessRecord( String identity, long indexPointer, boolean active, long dataPointer, long valuePointer, int length, int maxSize )
    {
        this.identity = identity;
        this.indexPointer = indexPointer;
        this.active = active;
        this.dataPointer = dataPointer;
        this.valuePointer = valuePointer;
        dataLength = length;
        this.maxSize = maxSize;
    }


    public long getIndexPointer()
    {
        return indexPointer;
    }

    public long getDataPointer()
    {
        return dataPointer;
    }

    public long getValuePointer()
    {
        return valuePointer;
    }

    public int getDataLength()
    {
        return dataLength;
    }

    public String getIdentity()
    {
        return identity;
    }

    public boolean isActive()
    {
        return active;
    }

    public int getMaxSize()
    {
        return maxSize;
    }
}
