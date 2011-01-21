package org.qi4j.index.solr.internal;

import org.apache.solr.analysis.BaseTokenizerFactory;

import java.io.Reader;

public class SingleTokenTokenizerFactory
      extends BaseTokenizerFactory
{
   public SingleTokenTokenizer create( Reader input )
   {
      return new SingleTokenTokenizer( input );
   }
}
