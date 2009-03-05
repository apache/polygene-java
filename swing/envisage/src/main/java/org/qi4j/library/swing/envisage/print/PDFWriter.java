/*  Copyright 2009 Tonny Kohar.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.envisage.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.edit.PDPageContentStream;
import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.pdmodel.font.PDType1Font;
import org.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.qi4j.library.swing.envisage.graph.GraphDisplay;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;

/**
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class PDFWriter
{
    protected PDDocument doc = null;
    protected PDPageContentStream curContentStream = null;
    
    protected PDFont normalFont = PDType1Font.HELVETICA;
    protected PDFont header1Font = PDType1Font.HELVETICA_BOLD;
    protected PDFont header2Font = PDType1Font.HELVETICA_BOLD_OBLIQUE;
    protected float header1FontSize = 16;
    protected float header2FontSize = 14;
    protected float normalFontSize = 12;

    public void write ( Component parent, ApplicationDetailDescriptor descriptor, GraphDisplay graphDisplay)
    {
        JFileChooser fc = new JFileChooser( );
        PDFFileFilter pdfFileFilter = new PDFFileFilter();
        fc.setFileFilter(pdfFileFilter);

        int choice = fc.showSaveDialog(parent);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fc.getSelectedFile();
        String filename = file.toString();
        String ext = ".pdf";
        if (!filename.endsWith(ext)) {
            filename = filename + ext;
            file = new File(filename);
        }

        write(file, descriptor, graphDisplay);
    }


    public void write( File file, ApplicationDetailDescriptor descriptor, GraphDisplay graphDisplay )
    {

        // TODO add progress bar
        try
        {
            writeImpl( file, descriptor, graphDisplay );
        }
        catch (Exception ex)
        {
            ex.printStackTrace(  );
        }
        finally
        {
            // TODO close the progress bar
        }
    }

    protected void writeImpl( File file, ApplicationDetailDescriptor descriptor, GraphDisplay graphDisplay ) throws Exception
    {

        try
        {
            doc = new PDDocument();

            writeGraphPage( graphDisplay );
            writePage( descriptor );

            /*PDPage page = new PDPage();
            doc.addPage( page );

            PDFont font = PDType1Font.HELVETICA_BOLD;

            PDPageContentStream contentStream = new PDPageContentStream(doc, page);
            contentStream.beginText();
            contentStream.setFont( font, 12 );
            contentStream.moveTextPositionByAmount( 100, 700 );
            contentStream.drawString( "HELLO WORLD" );
            contentStream.endText();
            contentStream.close();
            */

            if (curContentStream != null)
            {
                curContentStream.close();
            }

            doc.save( new FileOutputStream(file) );
        }
        finally
        {
            if( doc != null )
            {
                doc.close();
            }
        }
    }

    private void writeGraphPage(GraphDisplay graphDisplay) throws Exception
    {
        BufferedImage img = graphDisplay.getOffscreenBuffer();
        int w = img.getWidth();
        int h = img.getHeight();

        // rotate the image, if necessary
        if (w > h)
        {
            BufferedImage tImg = new BufferedImage( h, w, img.getType());
            Graphics2D g2d = tImg.createGraphics();
            g2d.setPaint( Color.WHITE );
            g2d.fillRect( 0,0, w, h);

            //System.out.println(w +"," + h);

            // rotate
            double x = (h - w)/2.0;
            double y = (w - h)/2.0;
            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            at.rotate(Math.toRadians(90), w/2.0, h/2.0);
            g2d.drawRenderedImage(img, at);
            g2d.dispose();
            img = tImg;
            //ImageIO.write( tImg, "png", new File("/home/tonny/qi4j-testimage.png") );

        }

        PDPage page = new PDPage();
        doc.addPage( page );

        PDRectangle pdRect = page.getArtBox();
        //float pW = pdRect.getWidth();
        //float pH = pdRect.getHeight();
        //System.out.println("pSize: " + pW + "," + pH);

        double scale = scaleToFit( img.getWidth(), img.getHeight(), pdRect.getWidth(), pdRect.getHeight() );
        if (scale != 1)
        {
            w = (int)Math.round( (img.getWidth() * scale) + .5 );
            h = (int)Math.round( (img.getHeight() * scale) + .5 );
            BufferedImage tImg = new BufferedImage( w, h,  img.getType());
            Graphics2D g2d = tImg.createGraphics();
            g2d.setPaint( Color.WHITE );
            g2d.fillRect( 0,0, w, h);

            AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
            g2d.drawRenderedImage(img, at);
            g2d.dispose();
            img = tImg;
        }

        PDJpeg xImage = new PDJpeg( doc, img);

        PDPageContentStream contentStream = new PDPageContentStream( doc, page );
        contentStream.drawImage( xImage, (pdRect.getWidth()-img.getWidth())/2 ,  (pdRect.getHeight()-img.getHeight())/2 );
        contentStream.close();
    }

    private void writePage(ApplicationDetailDescriptor descriptor) throws Exception
    {
        curContentStream = createNewPage();
        curContentStream.setFont( header1Font, header1FontSize );
        curContentStream.drawString( descriptor.toString() );

        //curContentStream.draw
        //descriptor.layers();
         //buildLayersNode( node, descriptor.layers() );
    }

    private PDPageContentStream createNewPage() throws Exception
    {
        if (curContentStream != null)
        {
            curContentStream.close();
        }

        PDPage page = new PDPage();
        doc.addPage( page );

        PDPageContentStream contentStream = new PDPageContentStream(doc, page);

        PDRectangle pdRect = page.getArtBox();
        System.out.println("pSize: " + pdRect.getWidth() + "," + pdRect.getHeight());
        
        contentStream.beginText();
        contentStream.moveTextPositionByAmount( 20, pdRect.getHeight() - 40);

        return contentStream;
    }

    private double scaleToFit (double srcW, double srcH, double destW, double destH)
    {
        double scale = 1;
        if (srcW > srcH) {
            if (srcW > destW) {
                scale = destW / srcW;
            }
            srcH = srcH * scale;
            if (srcH > destH) {
                scale = scale * (destH / srcH);
            }
        } else {
            if (srcH > destH) {
                scale = destH / srcH;
            }
            srcW = srcW * scale;
            if (srcW > destW ) {
                scale = scale * (destW / srcW);
            }
        }
        return scale;
    }

    class PDFFileFilter extends FileFilter
    {

        private String description;
        protected String extension = null;

        public PDFFileFilter()
        {
            extension = "pdf";
            description = "PDF - Portable Document Format";
        }

        public boolean accept( File f )
        {
            if( f != null )
            {
                if( f.isDirectory() )
                {
                    return true;
                }
                String str = getExtension( f );
                if( str != null && str.equals( extension ) )
                {
                    return true;
                }
            }
            return false;
        }

        public String getExtension( File f )
        {
            if( f != null )
            {
                String filename = f.getName();
                int i = filename.lastIndexOf( '.' );
                if( i > 0 && i < filename.length() - 1 )
                {
                    return filename.substring( i + 1 ).toLowerCase();
                }
            }
            return null;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
