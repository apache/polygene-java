/*
 * Copyright (c) 2009, Tony Kohar. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.envisage.print;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.DependencyDescriptor;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.envisage.graph.GraphDisplay;
import org.qi4j.envisage.util.TableRow;
import org.qi4j.envisage.util.TableRowUtilities;
import org.qi4j.tools.model.descriptor.*;
import org.qi4j.tools.model.util.DescriptorUtilities;

public class PDFWriter
{
    protected PDDocument doc = null;
    protected PDPageContentStream curContentStream = null;
    protected PDRectangle curPageSize;
    protected float curY;
    protected PDFont curFont;
    protected float curFontSize;

    protected String APPLICATION = "Application";
    protected String LAYER = "Layer";
    protected String MODULE = "Module";

    protected PDFont normalFont = PDType1Font.HELVETICA;
    protected PDFont header1Font = PDType1Font.HELVETICA_BOLD;  // Application
    protected PDFont header2Font = PDType1Font.HELVETICA_BOLD;  // Layer
    protected PDFont header3Font = PDType1Font.HELVETICA_BOLD; // Module
    protected PDFont header4Font = PDType1Font.HELVETICA_BOLD; // Type Container
    protected PDFont header5Font = PDType1Font.HELVETICA_BOLD_OBLIQUE; // Type
    protected float normalFontSize = 10;
    protected float header1FontSize = 18;
    protected float header2FontSize = 16;
    protected float header3FontSize = 14;
    protected float header4FontSize = 12;
    protected float header5FontSize = 12;

    protected float startX = 40;
    protected float startY = 40;
    protected float lineSpace = 15;
    protected float headerLineSpace = 25;

    public void write( Component parent, ApplicationDetailDescriptor descriptor, List<GraphDisplay> graphDisplays )
    {
        JFileChooser fc = new JFileChooser();
        PDFFileFilter pdfFileFilter = new PDFFileFilter();
        fc.setFileFilter( pdfFileFilter );

        int choice = fc.showSaveDialog( parent );
        if( choice != JFileChooser.APPROVE_OPTION )
        {
            return;
        }

        File file = fc.getSelectedFile();
        String filename = file.toString();
        String ext = ".pdf";
        if( !filename.endsWith( ext ) )
        {
            filename = filename + ext;
            file = new File( filename );
        }

        write( file, descriptor, graphDisplays );
    }

    public void write( File file, ApplicationDetailDescriptor descriptor, List<GraphDisplay> graphDisplays )
    {
        try
        {
            writeImpl( file, descriptor, graphDisplays );
        }
        catch( IOException | COSVisitorException ex )
        {
            ex.printStackTrace();
        }
    }

    protected void writeImpl( File file, ApplicationDetailDescriptor descriptor, List<GraphDisplay> graphDisplays )
        throws IOException, COSVisitorException
    {
        try
        {
            doc = new PDDocument();
            for( GraphDisplay graphDisplay : graphDisplays )
            {
                writeGraphPage( graphDisplay );
            }
            writePage( descriptor );
            if( curContentStream != null )
            {
                curContentStream.close();
                curContentStream = null;
            }
            doc.save( new FileOutputStream( file ) );
        }
        finally
        {
            if( curContentStream != null )
            {
                curContentStream.close();
                curContentStream = null;
            }

            if( doc != null )
            {
                doc.close();
                doc = null;
            }
        }
    }

    private void writeGraphPage( GraphDisplay graphDisplay )
        throws IOException
    {
        File tFile = File.createTempFile( "envisage", "png" );
        graphDisplay.saveImage( new FileOutputStream( tFile ), "png", 1d );

        BufferedImage img = ImageIO.read( tFile );

        int w = img.getWidth();
        int h = img.getHeight();

        int inset = 40;
        PDRectangle pdRect = new PDRectangle( w + inset, h + inset );
        PDPage page = new PDPage();
        page.setMediaBox( pdRect );
        doc.addPage( page );

        PDJpeg xImage = new PDJpeg( doc, img );

        PDPageContentStream contentStream = new PDPageContentStream( doc, page );
        contentStream.drawImage( xImage, ( pdRect.getWidth() - w ) / 2, ( pdRect.getHeight() - h ) / 2 );
        contentStream.close();
    }

    private void writePage( ApplicationDetailDescriptor descriptor )
    {
        createNewPage();
        setFont( header1Font, header1FontSize );
        writeString( APPLICATION + " : " + descriptor.toString() );

        writeLayersPage( descriptor.layers() );
    }

    private void writeLayersPage( Iterable<LayerDetailDescriptor> iter )
    {
        for( LayerDetailDescriptor descriptor : iter )
        {
            setFont( header2Font, header2FontSize );
            writeString( LAYER + " : " + descriptor.toString(), headerLineSpace );

            writeModulesPage( descriptor.modules() );
        }
    }

    private void writeModulesPage( Iterable<ModuleDetailDescriptor> iter )
    {
        for( ModuleDetailDescriptor descriptor : iter )
        {
            setFont( header3Font, header3FontSize );
            writeString( MODULE + " : " + descriptor.toString(), headerLineSpace );

            writeServicesPage( descriptor.services() );
            writeImportedServicesPage( descriptor.importedServices() );
            writeEntitiesPage( descriptor.entities() );
            writeTransientsPage( descriptor.composites() );
            writeValuesPage( descriptor.values() );
            writeObjectsPage( descriptor.objects() );
        }
    }

    private void writeServicesPage( Iterable<ServiceDetailDescriptor> iter )
    {
        for( ServiceDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeDependenciesPage( descriptor );
            writeTypeMethodsPage( descriptor );
            writeTypeStatesPage( descriptor );
            writeTypeServiceConfigurationPage( descriptor );
            writeTypeServiceUsagePage( descriptor );
        }
    }

    private void writeImportedServicesPage( Iterable<ImportedServiceDetailDescriptor> iter )
    {
        for( ImportedServiceDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeMethodsPage( descriptor );
            writeTypeServiceUsagePage( descriptor );
            writeTypeImportedByPage( descriptor );
        }
    }

    private void writeEntitiesPage( Iterable<EntityDetailDescriptor> iter )
    {
        for( EntityDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeDependenciesPage( descriptor );
            writeTypeMethodsPage( descriptor );
            writeTypeStatesPage( descriptor );
        }
    }

    private void writeTransientsPage( Iterable<CompositeDetailDescriptor> iter )
    {
        for( CompositeDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeDependenciesPage( descriptor );
            writeTypeMethodsPage( descriptor );
            writeTypeStatesPage( descriptor );
        }
    }

    private void writeValuesPage( Iterable<ValueDetailDescriptor> iter )
    {
        for( ValueDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeDependenciesPage( descriptor );
            writeTypeMethodsPage( descriptor );
            writeTypeStatesPage( descriptor );
        }
    }

    private void writeObjectsPage( Iterable<ObjectDetailDescriptor> iter )
    {
        for( ObjectDetailDescriptor descriptor : iter )
        {
            setFont( header4Font, header4FontSize );
            writeString( descriptor.toString(), headerLineSpace );
            writeTypeGeneralPage( descriptor );
            writeTypeDependenciesPage( descriptor );
            // object don't have methods
        }
    }

    private void writeTypeGeneralPage( Object objectDesciptor )
    {

        setFont( header5Font, header5FontSize );
        writeString( "General: ", headerLineSpace );

        setFont( normalFont, normalFontSize );

        if( objectDesciptor instanceof ServiceDetailDescriptor )
        {
            ServiceDescriptor descriptor = ( (ServiceDetailDescriptor) objectDesciptor ).descriptor();
            writeString( "- identity: " + descriptor.identity() );
            writeString( "- class: " + descriptor.toString() );
            writeString( "- visibility: " + descriptor.visibility().toString() );
            writeString( "- startup: " + ( (ServiceDetailDescriptor) objectDesciptor ).descriptor()
                .isInstantiateOnStartup() );
        }
        else if( objectDesciptor instanceof EntityDetailDescriptor )
        {
            EntityDescriptor descriptor = ( (EntityDetailDescriptor) objectDesciptor ).descriptor();
            writeString( "- name: " + descriptor.toString() );
            writeString( "- class: " + descriptor.toString() );
            writeString( "- visibility: " + descriptor.visibility().toString() );
        }
        else if( objectDesciptor instanceof ValueDetailDescriptor )
        {
            ValueDescriptor descriptor = ( (ValueDetailDescriptor) objectDesciptor ).descriptor();
            writeString( "- name: " + descriptor.toString() );
            writeString( "- class: " + descriptor.toString() );
            writeString( "- visibility: " + descriptor.visibility().toString() );
        }
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            ObjectDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor ).descriptor();
            writeString( "- name: " + descriptor.toString() );
            writeString( "- class: " + descriptor.toString() );
            writeString( "- visibility: " + descriptor.visibility().toString() );
        }
        else if( objectDesciptor instanceof CompositeDetailDescriptor )
        {
            CompositeDescriptor descriptor = ( (CompositeDetailDescriptor) objectDesciptor ).descriptor();
            writeString( "- name: " + descriptor.toString() );
            writeString( "- class: " + descriptor.toString() );
            writeString( "- visibility: " + descriptor.visibility().toString() );
        }
    }

    private void writeTypeDependenciesPage( Object objectDesciptor )
    {
        setFont( header5Font, header5FontSize );
        writeString( "Dependencies: ", headerLineSpace );

        if( objectDesciptor instanceof CompositeDetailDescriptor )
        {
            CompositeDetailDescriptor descriptor = (CompositeDetailDescriptor) objectDesciptor;
            Iterable<MixinDetailDescriptor> iter = descriptor.mixins();
            for( MixinDetailDescriptor mixinDescriptor : iter )
            {
                writeTypeDependenciesPage( mixinDescriptor.injectedFields() );
            }
        }
        else if( objectDesciptor instanceof ObjectDetailDescriptor )
        {
            ObjectDetailDescriptor descriptor = ( (ObjectDetailDescriptor) objectDesciptor );
            writeTypeDependenciesPage( descriptor.injectedFields() );
        }
    }

    private void writeTypeDependenciesPage( Iterable<InjectedFieldDetailDescriptor> iter )
    {
        setFont( normalFont, normalFontSize );
        for( InjectedFieldDetailDescriptor descriptor : iter )
        {
            DependencyDescriptor dependencyDescriptor = descriptor.descriptor().dependency();
            writeString( "- name: " + dependencyDescriptor.injectedClass().getSimpleName() );
            writeString( "    * annotation: @" + dependencyDescriptor.injectionAnnotation()
                .annotationType()
                .getSimpleName() );
            writeString( "    * optional: " + Boolean.toString( dependencyDescriptor.optional() ) );
            writeString( "    * type: " + dependencyDescriptor.injectionType().getClass().getSimpleName() );
        }
    }

    private void writeTypeMethodsPage( Object objectDesciptor )
    {
        if( !CompositeDetailDescriptor.class.isAssignableFrom( objectDesciptor.getClass() ) )
        {
            return;
        }

        setFont( header5Font, header5FontSize );
        writeString( "Methods: ", headerLineSpace );
        setFont( normalFont, normalFontSize );

        CompositeDetailDescriptor descriptor = (CompositeDetailDescriptor) objectDesciptor;
        List<CompositeMethodDetailDescriptor> list = DescriptorUtilities.findMethod( descriptor );

        HashSet<String> imports = new HashSet<>();
        for( CompositeMethodDetailDescriptor methodDescriptor : list )
        {
            addImport( imports, methodDescriptor.descriptor().method().getGenericReturnType() );
            for( Class parameter : methodDescriptor.descriptor().method().getParameterTypes() )
            {
                addImport( imports, parameter );
            }
        }
        for( String imp : imports )
        {
            writeString( "    import " + imp + ";" );
        }
        writeString( "" );

        for( CompositeMethodDetailDescriptor methodDescriptor : list )
        {
            Type returnType = methodDescriptor.descriptor().method().getGenericReturnType();
            writeString( "    " + formatType( returnType ) + "."
                         + methodDescriptor.toString()
                         + formatParameters( methodDescriptor.descriptor().method().getParameterTypes() )
            );
        }
    }

    private String formatParameters( Class<?>[] parameterTypes )
    {
        StringBuilder result = new StringBuilder();
        result.append( "(" );
        boolean first = true;
        int count = 1;
        for( Class parameter : parameterTypes )
        {
            if( !first )
            {
                result.append( "," );
            }
            first = false;
            result.append( " " );
            result.append( formatType( parameter ) );
            result.append( " " );
            result.append( "p" );
            result.append( count++ );
        }
        if( first )
        {
            // No parameters appended.
            result.append( ");" );
        }
        else
        {
            result.append( " );" );
        }
        return result.toString();
    }

    private String formatType( Type type )
    {
        if( type instanceof Class )
        {
            Class clazz = (Class) type;
            return clazz.getSimpleName();
        }
        else if( type instanceof ParameterizedType )
        {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] actuals = pType.getActualTypeArguments();
            Type ownerType = pType.getOwnerType();
            Type rawType = pType.getRawType();
            StringBuilder result = new StringBuilder();
            result.append( ( (Class) rawType ).getSimpleName() );
            result.append( "<" );
            boolean first = true;
            for( Type actual : actuals )
            {
                if( !first )
                {
                    result.append( "," );
                }
                first = false;
                result.append( formatType( actual ) );
            }
            result.append( ">" );

            return result.toString();
        }
        else if( type instanceof WildcardType )
        {
            // TODO: I am sure there are other wildcard constructs that will format incorrectly. Fix that!
            //
            WildcardType wildcard = (WildcardType) type;
            Type[] lowers = wildcard.getLowerBounds();
            Type[] uppers = wildcard.getUpperBounds();
            StringBuilder result = new StringBuilder();
            result.append( "? extends " );
            boolean first = true;
            for( Type upper : uppers )
            {
                if( !first )
                {
                    result.append( ", " );
                }
                result.append( formatType( upper ) );
            }
            return result.toString();
        }
        else if( type instanceof TypeVariable )
        {
            return type.toString();
        }

        return type.toString();
    }

    private void addImport( HashSet<String> imports, Type type )
    {
        if( type instanceof Class )
        {
            Class clazz = (Class) type;
            Package pkkage = clazz.getPackage();
            if( pkkage == null )
            {
                return;
            }
            String packageName = pkkage.getName();
            if( packageName.startsWith( "java" ) )
            {
                return;
            }
            imports.add( clazz.getName() );
        }
        else if( type instanceof ParameterizedType )
        {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] actuals = pType.getActualTypeArguments();
            Type ownerType = pType.getOwnerType();
            Type rawType = pType.getRawType();
            addImport( imports, ownerType );
            addImport( imports, rawType );
            for( Type actual : actuals )
            {
                addImport( imports, actual );
            }
        }
    }

    private void writeTypeStatesPage( Object objectDesciptor )
    {
        if( !CompositeDetailDescriptor.class.isAssignableFrom( objectDesciptor.getClass() ) )
        {
            return;
        }

        setFont( header5Font, header5FontSize );
        writeString( "States: ", headerLineSpace );

        CompositeDetailDescriptor descriptor = (CompositeDetailDescriptor) objectDesciptor;
        List<CompositeMethodDetailDescriptor> list = DescriptorUtilities.findState( descriptor );

        setFont( normalFont, normalFontSize );
        for( CompositeMethodDetailDescriptor methodDescriptor : list )
        {
            writeString( "- name: " + methodDescriptor.toString() );
            writeString( "    * return: " + methodDescriptor.descriptor().method().getGenericReturnType() );
        }
    }

    private void writeTypeServiceConfigurationPage( Object objectDesciptor )
    {
        setFont( header5Font, header5FontSize );
        writeString( "Configuration: ", headerLineSpace );

        Object configDescriptor = DescriptorUtilities.findServiceConfiguration( (ServiceDetailDescriptor) objectDesciptor );

        if( configDescriptor == null )
        {
            return;
        }

        ModelDescriptor spiDescriptor;
        String typeString;
        if( configDescriptor instanceof ServiceDetailDescriptor )
        {
            spiDescriptor = ( (ServiceDetailDescriptor) configDescriptor ).descriptor();
            typeString = "Service";
        }
        else if( configDescriptor instanceof EntityDetailDescriptor )
        {
            spiDescriptor = ( (EntityDetailDescriptor) configDescriptor ).descriptor();
            typeString = "Entity";
        }
        else if( configDescriptor instanceof ValueDetailDescriptor )
        {
            spiDescriptor = ( (ValueDetailDescriptor) configDescriptor ).descriptor();
            typeString = "Value";
        }
        else if( configDescriptor instanceof ObjectDetailDescriptor )
        {
            spiDescriptor = ( (ObjectDetailDescriptor) configDescriptor ).descriptor();
            typeString = "Object";
        }
        else if( configDescriptor instanceof CompositeDetailDescriptor )
        {
            spiDescriptor = ( (CompositeDetailDescriptor) configDescriptor ).descriptor();
            typeString = "Transient";
        }
        else
        {
            throw new PrintingException( "Unknown configuration descriptor: " + configDescriptor.getClass()
                .getName(), null );
        }

        setFont( normalFont, normalFontSize );
        writeString( "- name: " + spiDescriptor.toString() );
        writeString( "- class: " + spiDescriptor.toString() );
        writeString( "- type: " + typeString );
    }

    private void writeTypeServiceUsagePage( Object objectDesciptor )
    {
        setFont( header5Font, header5FontSize );
        writeString( "Usage: ", headerLineSpace );

        setFont( normalFont, normalFontSize );
        List<ServiceUsage> serviceUsages = DescriptorUtilities.findServiceUsage( (ServiceDetailDescriptor) objectDesciptor );
        List<TableRow> rows = TableRowUtilities.toTableRows( serviceUsages );
        for( TableRow row : rows )
        {

            //String owner;
            String usage;
            String module;
            String layer;

            Object obj = row.get( 0 );
            if( obj instanceof CompositeDetailDescriptor )
            {
                CompositeDetailDescriptor descriptor = (CompositeDetailDescriptor) obj;
                //owner = descriptor.toString();
                module = descriptor.module().toString();
                layer = descriptor.module().layer().toString();
            }
            else
            {
                ObjectDetailDescriptor descriptor = (ObjectDetailDescriptor) obj;
                //owner = descriptor.toString();
                module = descriptor.module().toString();
                layer = descriptor.module().layer().toString();
            }

            InjectedFieldDetailDescriptor injectedFieldescriptor = (InjectedFieldDetailDescriptor) row.get( 1 );
            DependencyDescriptor dependencyDescriptor = injectedFieldescriptor.descriptor().dependency();
            Annotation annotation = dependencyDescriptor.injectionAnnotation();
            usage = injectedFieldescriptor.toString() + " (@" + annotation.annotationType().getSimpleName() + ")";

            writeString( "- owner: " + row.get( 0 ).toString() );
            writeString( "    * usage: " + usage );
            writeString( "    * module: " + module );
            writeString( "    * layer: " + layer );
        }
    }

    private void writeTypeImportedByPage( Object objectDesciptor )
    {
        setFont( header5Font, header5FontSize );
        writeString( "Imported by: ", headerLineSpace );

        ImportedServiceDetailDescriptor detailDescriptor = (ImportedServiceDetailDescriptor) objectDesciptor;
        ImportedServiceDescriptor descriptor = detailDescriptor.descriptor().importedService();
        Class<? extends ServiceImporter> importer = descriptor.serviceImporter();

        setFont( normalFont, normalFontSize );
        writeString( "- name: " + importer.getSimpleName() );
        writeString( "- class: " + importer.toString() );
    }

    private void writeString( String text )
    {
        writeString( text, this.lineSpace );
    }

    private void writeString( String text, float lineSpace )
    {
        // check for page size, if necessary create new page
        if( ( curY - lineSpace ) <= startY )
        {
            //System.out.println("new line: " + curY + " - " + lineSpace + " = " + (curY-lineSpace) );
            createNewPage();
        }

        curY = curY - lineSpace;

        try
        {
            curContentStream.moveTextPositionByAmount( 0, -lineSpace );
            curContentStream.drawString( text );
        }
        catch( IOException e )
        {
            throw new PrintingException( "Unable to write string: " + text, e );
        }
    }

    private void setFont( PDFont font, float fontSize )
    {
        curFont = font;
        curFontSize = fontSize;
        try
        {
            curContentStream.setFont( curFont, curFontSize );
        }
        catch( IOException e )
        {
            throw new PrintingException( "Unable to set font: " + font.toString() + ", " + fontSize + "pt", e );
        }
    }

    private void createNewPage()
    {
        try
        {
            if( curContentStream != null )
            {
                curContentStream.endText();
                curContentStream.close();
            }

            PDPage page = new PDPage();
            doc.addPage( page );

            curContentStream = new PDPageContentStream( doc, page );

            curPageSize = page.getArtBox();
            //System.out.println("pSize: " + pdRect.getWidth() + "," + pdRect.getHeight());

            curContentStream.beginText();
            curY = curPageSize.getHeight() - startY;
            curContentStream.moveTextPositionByAmount( startX, curY );

            if( curFont != null )
            {
                setFont( curFont, curFontSize );
            }
        }
        catch( IOException e )
        {
            throw new PrintingException( "Unable to create page.", e );
        }
    }

    private double scaleToFit( double srcW, double srcH, double destW, double destH )
    {
        double scale = 1;
        if( srcW > srcH )
        {
            if( srcW > destW )
            {
                scale = destW / srcW;
            }
            srcH = srcH * scale;
            if( srcH > destH )
            {
                scale = scale * ( destH / srcH );
            }
        }
        else
        {
            if( srcH > destH )
            {
                scale = destH / srcH;
            }
            srcW = srcW * scale;
            if( srcW > destW )
            {
                scale = scale * ( destW / srcW );
            }
        }
        return scale;
    }

    static class PDFFileFilter
        extends FileFilter
    {

        private final String description;
        protected String extension = null;

        public PDFFileFilter()
        {
            extension = "pdf";
            description = "PDF - Portable Document Format";
        }

        @Override
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

        @Override
        public String getDescription()
        {
            return description;
        }
    }

}
