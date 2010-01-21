/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.annotation.internal;

import java.io.StringReader;
import java.util.Collection;

import org.xwiki.annotation.Annotation;
import org.xwiki.annotation.AnnotationService;
import org.xwiki.annotation.AnnotationServiceException;
import org.xwiki.annotation.io.IOService;
import org.xwiki.annotation.io.IOServiceException;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.annotation.renderer.AnnotationPrintRenderer;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.transformation.TransformationManager;

/**
 * Default annotation service, using the default {@link IOTargetService} and and {@link IOTargetService}, dispatching
 * calls and implementing the rendering of the content based on these data from the 2 services.
 * 
 * @version $Id$
 */
@Component
public class DefaultAnnotationService implements AnnotationService
{
    /**
     * The storage service for annotations.
     */
    @Requirement
    private IOService ioService;

    /**
     * Component manager used to lookup the content alterer needed for the specific document.
     */
    @Requirement
    private ComponentManager componentManager;

    /**
     * The storage service for annotation targets (documents).
     */
    @Requirement
    private IOTargetService targetIoService;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.target.AnnotationTarget#addAnnotation(String, String, String, int, String, String)
     */
    public void addAnnotation(String target, String selection, String selectionContext, int offset, String user,
        String metadata) throws AnnotationServiceException
    {
        try {
            // create the annotation with this data and send it to the storage service
            // TODO: also think of mapping the annotation on the document at add time and fail it if it's not mappable,
            // for extra security
            Annotation annotation = new Annotation(target, user, metadata, selection, selectionContext, "");
            ioService.addAnnotation(target, annotation);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException("An exception occurred when accessing the storage services", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotatedRenderedContent(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public String getAnnotatedRenderedContent(String sourceReference, String sourceSyntax, String outputSyntax)
        throws AnnotationServiceException
    {
        try {
            String source = targetIoService.getSource(sourceReference);
            String sourceSyntaxId = sourceSyntax;
            // get if unspecified, get the source from the io service
            if (sourceSyntaxId == null) {
                sourceSyntaxId = targetIoService.getSourceSyntax(sourceReference);
            }

            Parser parser = componentManager.lookup(Parser.class, sourceSyntaxId);
            XDOM xdom = parser.parse(new StringReader(source));

            // run transformations
            SyntaxFactory syntaxFactory = componentManager.lookup(SyntaxFactory.class);
            Syntax sSyntax = syntaxFactory.createSyntaxFromIdString(sourceSyntaxId);
            TransformationManager transformationManager = componentManager.lookup(TransformationManager.class);
            transformationManager.performTransformations(xdom, sSyntax);

            // build the annotations renderer hint for the specified output syntax
            String outputSyntaxId = "annotations-" + outputSyntax;
            AnnotationPrintRenderer annotationsRenderer =
                componentManager.lookup(AnnotationPrintRenderer.class, outputSyntaxId);
            WikiPrinter printer = new DefaultWikiPrinter();
            annotationsRenderer.setPrinter(printer);
            // set the annotations for this renderer
            annotationsRenderer.setAnnotations(ioService.getValidAnnotations(sourceReference));

            xdom.traverse(annotationsRenderer);

            return printer.toString();
        } catch (Exception exc) {
            throw new AnnotationServiceException(exc);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotatedHTML(String)
     */
    public String getAnnotatedHTML(String sourceReference) throws AnnotationServiceException
    {
        return getAnnotatedRenderedContent(sourceReference, null, "xhtml/1.0");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getAnnotations(String)
     */
    public Collection<Annotation> getAnnotations(String target) throws AnnotationServiceException
    {
        try {
            return ioService.getAnnotations(target);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#getValidAnnotations(String)
     */
    public Collection<Annotation> getValidAnnotations(String target) throws AnnotationServiceException
    {
        try {
            return ioService.getValidAnnotations(target);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.annotation.AnnotationService#removeAnnotation(String, String)
     */
    public void removeAnnotation(String target, String annotationID) throws AnnotationServiceException
    {
        try {
            ioService.removeAnnotation(target, annotationID);
        } catch (IOServiceException e) {
            throw new AnnotationServiceException(e.getMessage());
        }
    }
}
