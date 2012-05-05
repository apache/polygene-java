package com.marcgrue.dcisample_a.bootstrap;

import com.marcgrue.dcisample_a.bootstrap.assembly.Assembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.envisage.Envisage;
import org.qi4j.spi.structure.ApplicationModelSPI;

/**
 * Visualize the application assemblage structure.
 */
public class VisualizeApplicationStructure
{
   public static void main( String[] args ) throws Exception
   {
      Energy4Java qi4j = new Energy4Java();
      Assembler assembler = new Assembler();
      ApplicationModelSPI applicationModel = qi4j.newApplicationModel( assembler );
      applicationModel.newInstance( qi4j.spi() );

      /*
      * The Envisage Swing app visualizes the application assemblage structure.
      *
      * Tree view:
      * - Click on elements to expand sub-elements.
      * - Scroll to change font size.
      * - Right click on viewer to re-size to fit window.
      *
      * Stacked view:
      * - Scroll to zoom in/out of structure levels - might freeze though :-(
      *
      * Click on any element and see details of that element in the upper right pane.
      *
      * Pretty cool, eh?
      * */
      new Envisage().run( applicationModel );
      int randomTimeoutMs = 18374140;
      Thread.sleep( randomTimeoutMs );
   }
}