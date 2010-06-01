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
package org.xwiki.officepreview;

import java.util.Arrays;
import java.util.List;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.logging.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Exposes office preview utility methods to velocity scripts.
 * 
 * @since 2.4M1
 * @version $Id$
 */
public class OfficePreviewVelocityBridge
{
    /**
     * File extensions corresponding to presentations.
     */
    private static final List<String> PRESENTATION_FORMAT_EXTENSIONS = Arrays.asList("ppt", "pptx", "odp");

    /**
     * The key used to place any error messages while previewing office documents.
     */
    private static final String OFFICE_PREVIEW_ERROR = "OFFICE_PREVIEW_ERROR";

    /**
     * Used to lookup various renderer implementations.
     */
    private ComponentManager componentManager;

    /**
     * Logging support.
     */
    private Logger logger;

    /**
     * Reference to current execution.
     */
    private Execution execution;

    /**
     * Used for constructing {@link AttachmentReference} instances from strings.
     */
    private AttachmentReferenceResolver<String> resolver;

    /**
     * Used to query current document syntax.
     */
    private DocumentAccessBridge docBridge;

    /**
     * Constructs a new bridge instance.
     * 
     * @param componentManager used to lookup for other required components.
     * @param logger for logging support.
     * @throws Exception if an error occurs while initializing bridge.
     */
    @SuppressWarnings("unchecked")
    public OfficePreviewVelocityBridge(ComponentManager componentManager, Logger logger) throws Exception
    {
        // Base components.
        this.componentManager = componentManager;
        this.logger = logger;

        // Lookup other required components.
        this.execution = componentManager.lookup(Execution.class);
        this.resolver = componentManager.lookup(AttachmentReferenceResolver.class);
        this.docBridge = componentManager.lookup(DocumentAccessBridge.class);
    }

    /**
     * Builds a preview of the specified office attachment in xhtml/1.0 syntax.
     * 
     * @param attachmentReferenceString string identifying the office attachment.
     * @param filterStyles whether office document styles should be filtered.
     * @return preview of the specified office attachment or null if an error occurs.
     */
    public String preview(String attachmentReferenceString, boolean filterStyles)
    {
        AttachmentReference attachmentReference = resolver.resolve(attachmentReferenceString);
        try {
            return preview(attachmentReference, filterStyles);
        } catch (Exception ex) {
            String message = "Could not preview office document [%s] - %s";
            message = String.format(message, attachmentReferenceString, ex.getMessage());
            setErrorMessage(message);
            logger.error(message, ex);
        }
        return null;
    }

    /**
     * Builds a preview of the specified office attachment in xhtml/1.0 syntax.
     * 
     * @param attachmentReference reference to the attachment to be previewed.
     * @param filterStyles whether office document styles should be filtered.
     * @return preview of the specified office attachment in xhtml/1.0 syntax.
     * @throws Exception if current user does not have enough privileges to view the requested attachment or if an error
     *             occurs while generating the preview.
     */
    private String preview(AttachmentReference attachmentReference, boolean filterStyles) throws Exception
    {
        // Check whether current user has view rights on the document containing the attachment.
        if (!docBridge.isDocumentViewable(attachmentReference.getDocumentReference())) {
            throw new Exception("Inadequate privileges.");
        }

        OfficePreviewBuilder builder = componentManager.lookup(OfficePreviewBuilder.class);      
        if (isPresentation(attachmentReference.getName())) {
            builder = componentManager.lookup(OfficePreviewBuilder.class, "presentation");
        }

        // Build the preview and render the result.
        return render(builder.build(attachmentReference, filterStyles), "xhtml/1.0");
    }

    /**
     * Utility method for checking if a file name corresponds to an office presentation.
     * 
     * @param fileName attachment file name.
     * @return true if the file name / extension represents an office presentation format.
     */
    private boolean isPresentation(String fileName)
    {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        return PRESENTATION_FORMAT_EXTENSIONS.contains(extension);
    }

    /**
     * Renders the given block into specified syntax.
     * 
     * @param block {@link Block} to be rendered.
     * @param syntaxId expected output syntax.
     * @return string holding the result of rendering.
     * @throws Exception if an error occurs during rendering.
     */
    private String render(Block block, String syntaxId) throws Exception
    {
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, syntaxId);
        renderer.render(block, printer);
        return printer.toString();
    }

    /**
     * @return an error message set inside current execution or null.
     */
    public String getErrorMessage()
    {
        return (String) execution.getContext().getProperty(OFFICE_PREVIEW_ERROR);
    }

    /**
     * Utility method for setting an error message inside current execution.
     * 
     * @param message error message.
     */
    private void setErrorMessage(String message)
    {
        execution.getContext().setProperty(OFFICE_PREVIEW_ERROR, message);
    }
}
