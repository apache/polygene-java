package org.qi4j.api.geometry;

import org.qi4j.api.common.Optional;
import org.qi4j.api.geometry.internal.TLinearRing;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;

import java.util.List;

@Mixins( TPolygon.Mixin.class )
public interface TPolygon extends TGeometry {


    Property<TLinearRing> shell();

    @Optional
    Property<List<TLinearRing>> holes();

    // Interaction
    TPolygon of(TLinearRing shell);
    TPolygon of(TLinearRing shell, @Optional TLinearRing... holes);


    public abstract class Mixin implements TPolygon
    {
        @Structure
        Module module;

        @This
        TPolygon self;

        public TPolygon of(TLinearRing shell)
        {
           return of(shell, null);
        }

        public TPolygon of(TLinearRing shell, TLinearRing... holes)
        {
            if (shell != null) {
                self.shell().set(shell);
            }

            if (holes != null && holes.length !=0) {
                for (TLinearRing hole : holes)
                    self.holes().get().add(hole);
            }

            self.type().set("Polygon");
            return self;
        }

    }
}
