package org.qi4j.index.solr.internal;

import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.AttributeSource;

import java.io.Reader;

public class SingleTokenTokenizer
      extends WhitespaceTokenizer
{
   public SingleTokenTokenizer( Reader in )
   {
      super( in );
   }

   public SingleTokenTokenizer( AttributeSource source, Reader in )
   {
      super( source, in );
   }

   public SingleTokenTokenizer( AttributeFactory factory, Reader in )
   {
      super( factory, in );
   }

   @Override
   protected boolean isTokenChar( char c )
   {
      return true;
   }
}
