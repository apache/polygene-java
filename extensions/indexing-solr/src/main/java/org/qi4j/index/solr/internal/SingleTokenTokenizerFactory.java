package org.qi4j.index.solr.internal;

import java.io.Reader;
import org.apache.solr.analysis.BaseTokenizerFactory;

public class SingleTokenTokenizerFactory
      extends BaseTokenizerFactory
{
   @Override
   public SingleTokenTokenizer create( Reader input )
   {
      return new SingleTokenTokenizer( input );
   }
}
