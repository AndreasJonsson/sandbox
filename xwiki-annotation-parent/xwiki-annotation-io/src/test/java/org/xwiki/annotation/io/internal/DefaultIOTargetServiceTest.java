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
package org.xwiki.annotation.io.internal;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.annotation.io.IOTargetService;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Tests the default implementation of {@link IOTargetService}, and integration with target resolvers, up to the
 * document access bridge access.
 * 
 * @version $Id$
 */
@RunWith(JMock.class)
public class DefaultIOTargetServiceTest extends AbstractComponentTestCase
{
    /**
     * Tested io target service.
     */
    private IOTargetService ioTargetService;

    /**
     * Mock for the document access bridge.
     */
    private DocumentAccessBridge dabMock;

    /**
     * Mockery to mock the document access bridge.
     */
    private Mockery mockery = new JUnit4Mockery();

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        // register the dab
        dabMock = mockery.mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptor =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptor.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptor, dabMock);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractComponentTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // get the default io target service
        ioTargetService = getComponentManager().lookup(IOTargetService.class);
    }

    @Test
    public void testGettersWhenTargetIsTypedDocument() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(dabMock).getDocumentContent("wiki:Space.Page");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("wiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "DOCUMENT://wiki:Space.Page";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGettersWhenTargetIsNonTypedDocument() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(dabMock).getDocumentContent("wiki:Space.Page");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("wiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "wiki:Space.Page";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGettersWhenTargetIsNonTypedRelativeDocument() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // default resolver should be used
                oneOf(dabMock).getDocumentContent("xwiki:Space.Page");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("xwiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "Space.Page";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGettersWhenTargetIsTypedRelativeDocument() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // default resolver should be used
                oneOf(dabMock).getDocumentContent("xwiki:Space.Page");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("xwiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "DOCUMENT://Space.Page";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGettersWhenTargetIsTypedSpace() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // default resolver should be used
                oneOf(dabMock).getDocumentContent("SPACE://wiki:Space");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("SPACE://wiki:Space");
                will(returnValue("xwiki/2.0"));
            }
        });

        // expect source ref to be used as is, as it doesn't parse to something acceptable
        String reference = "SPACE://wiki:Space";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGettersWhenTargetIsEmptyString() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // default resolver should be used. Note that this will fail if default values change, not very well
                // isolated
                oneOf(dabMock).getDocumentContent("xwiki:Main.WebHome");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("xwiki:Main.WebHome");
                will(returnValue("xwiki/2.0"));
            }
        });

        // expect source ref to be used as is, as it doesn't parse to something acceptable
        String reference = "";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGetterWhenTargetIsTypedIndexedObjectProperty() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(dabMock).getProperty("wiki:Space.Page", "XWiki.Class", 1, "property");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("wiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "OBJECT_PROPERTY://wiki:Space.Page^XWiki.Class[1].property";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGetterWhenTargetIsTypedDefaultObjectProperty() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(dabMock).getProperty("wiki:Space.Page", "XWiki.Class", "property");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("wiki:Space.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "OBJECT_PROPERTY://wiki:Space.Page^XWiki.Class.property";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGetterWhenTargetIsTypedObjectPropertyInRelativeDocument() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                oneOf(dabMock).getProperty("xwiki:Main.Page", "XWiki.Class", "property");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("xwiki:Main.Page");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "OBJECT_PROPERTY://Page^XWiki.Class.property";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }

    @Test
    public void testGetterWhenTargetIsNonTypedObjectProperty() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // target will be parsed as document, because document is the default
                oneOf(dabMock).getDocumentContent("wiki:Space\\.Page^XWiki\\.Class.property");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("wiki:Space\\.Page^XWiki\\.Class.property");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "wiki:Space.Page^XWiki.Class.property";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }
    
    @Test
    public void testGetterWhenTargetIsTypedIndexedRelativeObjectProperty() throws Exception
    {
        mockery.checking(new Expectations()
        {
            {
                // this will fail if defaults fail, not very well isolated
                oneOf(dabMock).getProperty("xwiki:Main.WebHome", "Classes.Class", 3, "property");
                will(returnValue("defcontent"));
                oneOf(dabMock).getDocumentSyntaxId("xwiki:Main.WebHome");
                will(returnValue("xwiki/2.0"));
            }
        });

        String reference = "OBJECT_PROPERTY://Classes.Class[3].property";
        assertEquals("defcontent", ioTargetService.getSource(reference));
        assertEquals("xwiki/2.0", ioTargetService.getSourceSyntax(reference));
    }    
}
