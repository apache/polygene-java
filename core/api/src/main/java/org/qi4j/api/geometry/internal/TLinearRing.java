/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
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

package org.qi4j.api.geometry.internal;

import org.qi4j.api.geometry.TLineString;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

@Mixins(TLinearRing.Mixin.class)
public interface TLinearRing extends TLineString {

    // TLinearRing of(TPoint... points);
    // TLinearRing of(List<TPoint> points);
    // TLinearRing xy(double x, double y);
    boolean isValid();

    public abstract class Mixin extends TLineString.Mixin implements TLinearRing //, TLineString
    {

        @This
        TLinearRing self;

        /**
         * @Override public TLinearRing of(TPoint... points)
         * {
         * super.of(points);
         * <p/>
         * // self.of(points);
         * <p/>
         * return self;
         * }
         * @Override public TLinearRing of(List<TPoint> points)
         * {
         * of(points.toArray(new TPoint[points.size()]));
         * return self;
         * }
         * @Override public TLinearRing xy(double x, double y) {
         * super.xy(x,y);
         * // self.xy(x,y);
         * return self;
         * }
         */

        // public Coordinate[] getCoordinates()
        //  {
        //    return null;
        //}
        @Override
        public boolean isValid() {
            if (self.getStartPoint() == null || self.getEndPoint() == null) return false;
            return self.getStartPoint().compareTo(self.getEndPoint()) == 0 ? true : false;
        }
    }

}
