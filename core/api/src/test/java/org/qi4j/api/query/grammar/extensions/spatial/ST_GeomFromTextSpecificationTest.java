package org.qi4j.api.query.grammar.extensions.spatial;

import org.junit.Test;
import org.qi4j.api.geometry.internal.TGeometry;
import org.qi4j.api.query.grammar.extensions.spatial.convert.ST_GeomFromTextSpecification;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import java.text.ParseException;

/**
 * Created by jj on 05.11.14.
 */
public class ST_GeomFromTextSpecificationTest  extends AbstractQi4jTest {




        public void assemble(ModuleAssembly module )
        throws AssemblyException
        {
           //  module.objects( A.class, B.class, C.class, D.class );
        }

    @Test
    public void whenWTKPoint() throws ParseException {
       //  new ST_GeomFromTextSpecification("POINT(3.139003 101.686854)", 1).convert();
        TGeometry geometry = new ST_GeomFromTextSpecification("POINT(49.550881 10.712809)", 1).convert(module);
    }


}
