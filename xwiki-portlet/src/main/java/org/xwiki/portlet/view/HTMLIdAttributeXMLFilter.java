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
package org.xwiki.portlet.view;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * An XML filter that rewrites all element identifiers to ensure they are unique in the context of a portal page.
 * 
 * @version $Id$
 */
public class HTMLIdAttributeXMLFilter extends XMLFilterImpl
{
    /**
     * The name of the id element attribute.
     */
    private static final String ID = "id";

    /**
     * The name of the anchor attribute holding the URL.
     */
    private static final String HREF = "href";

    /**
     * The name of the DIV HTML tag.
     */
    private static final String DIV = "div";

    /**
     * The string all element identifiers will be prefixed with.
     */
    private final String namespace;

    /**
     * Flag indicating if the output should be wrapped in a container that has the {@link #namespace} identifier or not.
     */
    private final boolean wrapOutput;

    /**
     * Creates a new XML filter that name-spaces all element identifiers.
     * 
     * @param namespace the string all element identifiers will be prefixed with
     * @param wrapOutput {@code true} to wrap the output in a container that has the {@code namespace} identifier,
     *            {@code false} otherwise
     */
    public HTMLIdAttributeXMLFilter(String namespace, boolean wrapOutput)
    {
        this.namespace = namespace;
        this.wrapOutput = wrapOutput;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();

        if (wrapOutput) {
            // Start the portlet output container.
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(null, ID, ID, "ID", namespace);
            super.startElement(null, DIV, DIV, attributes);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#startElement(String, String, String, Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        super.startElement(uri, localName, qName, rewriteURLFragment(rewriteId(atts)));
    }

    /**
     * Rewrites the id attribute if present in the given list of attributes.
     * 
     * @param atts the list of element attributes
     * @return the given list of attributes where the value of the id attribute had been changed
     */
    private Attributes rewriteId(Attributes atts)
    {
        String id = atts.getValue(ID);
        if (id != null) {
            AttributesImpl newAtts = atts instanceof AttributesImpl ? (AttributesImpl) atts : new AttributesImpl(atts);
            newAtts.setValue(atts.getIndex(ID), namespace(id));
            return newAtts;
        }
        return atts;
    }

    /**
     * Rewrites URL fragments in anchor URLs relative to the current page.
     * 
     * @param atts the lists of element attributes
     * @return the given list of attributes where the value of the {@link #HREF} attribute has been changed
     */
    private Attributes rewriteURLFragment(Attributes atts)
    {
        String href = atts.getValue(HREF);
        if (href != null && href.startsWith("#")) {
            AttributesImpl newAtts = atts instanceof AttributesImpl ? (AttributesImpl) atts : new AttributesImpl(atts);
            newAtts.setValue(atts.getIndex(HREF), String.format("#%s", namespace(href.substring(1))));
            return newAtts;
        }
        return atts;
    }

    /**
     * Name-spaces an element identifier.
     * 
     * @param id an element id
     * @return a new id that is unique in the context of the portal page
     */
    private String namespace(String id)
    {
        return namespace + "-" + id;
    }

    /**
     * {@inheritDoc}
     * 
     * @see XMLFilterImpl#endDocument()
     */
    @Override
    public void endDocument() throws SAXException
    {
        if (wrapOutput) {
            // End the portlet output container.
            super.endElement(null, DIV, DIV);
        }

        super.endDocument();
    }
}
