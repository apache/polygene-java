package com.marcgrue.dcisample_b.data.structure.location;

import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.constraints.annotation.Matches;

/**
 * UnLocode
 *
 * A United Nations location code.
 *
 * http://www.unece.org/cefact/locode/
 * http://www.unece.org/cefact/locode/DocColumnDescription.htm#LOCODE
 *
 * UnLocode is mandatory and immutable.
 */
public interface UnLocode
      extends ValueComposite
{
    // Country code is exactly two letters.
    // Location code is usually three letters, but may contain the numbers 2-9 as well
    @Matches( "[a-zA-Z]{2}[a-zA-Z2-9]{3}" )
    Property<String> code();
}
