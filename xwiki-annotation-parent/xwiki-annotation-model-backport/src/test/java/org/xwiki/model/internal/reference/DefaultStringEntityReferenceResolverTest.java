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
package org.xwiki.model.internal.reference;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelConfiguration;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;

/**
 * Unit tests for {@link DefaultStringEntityReferenceResolver}.
 * 
 * @version $Id$
 * @since 2.2M1
 */
public class DefaultStringEntityReferenceResolverTest
{
    private static final String DEFAULT_WIKI = "xwiki";
    
    private static final String DEFAULT_SPACE = "XWiki";
    
    private static final String DEFAULT_PAGE = "WebHome";
        
    private static final String DEFAULT_ATTACHMENT = "filename";

    private static final String DEFAULT_OBJECT = "defobject";

    private static final String DEFAULT_OBJECT_PROPERTY = "defproperty";

    private EntityReferenceResolver resolver;

    private Mockery mockery = new Mockery();

    private ModelConfiguration mockModelConfiguration;

    @Before
    public void setUp()
    {
        this.resolver = new DefaultStringEntityReferenceResolver();
        this.mockModelConfiguration = this.mockery.mock(ModelConfiguration.class);
        ReflectionUtils.setFieldValue(this.resolver, "configuration", this.mockModelConfiguration);
        
        this.mockery.checking(new Expectations() {{
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.WIKI);
                will(returnValue(DEFAULT_WIKI));
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.SPACE);
                will(returnValue(DEFAULT_SPACE));
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.DOCUMENT);
                will(returnValue(DEFAULT_PAGE));
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.ATTACHMENT);
                will(returnValue(DEFAULT_ATTACHMENT));
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.OBJECT);
                will(returnValue(DEFAULT_OBJECT));
            allowing(mockModelConfiguration).getDefaultReferenceValue(EntityType.OBJECT_PROPERTY);
                will(returnValue(DEFAULT_OBJECT_PROPERTY));
        }});
    }

    @Test
    public void testResolveDocumentReference() throws Exception
    {
        EntityReference reference = resolver.resolve("wiki:space.page", EntityType.DOCUMENT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.getName());

        reference = resolver.resolve("wiki:space.", EntityType.DOCUMENT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.getName());

        reference = resolver.resolve("space.", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.getName());

        reference = resolver.resolve("page", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.getName());

        reference = resolver.resolve(".", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.getName());

        reference = resolver.resolve(null, EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.getName());

        reference = resolver.resolve("", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.getName());

        reference = resolver.resolve("wiki1.wiki2:wiki3:some.space.page", EntityType.DOCUMENT);
        Assert.assertEquals("wiki1.wiki2:wiki3", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("some.space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.getName());

        reference = resolver.resolve("some.space.page", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("some.space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.getName());

        reference = resolver.resolve("wiki:page", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("wiki:page", reference.getName());

        // Test escapes

        reference = resolver.resolve("\\\\\\.:@\\.", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("\\.:@.", reference.getName());

        reference = resolver.resolve("some\\.space.page", EntityType.DOCUMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("some.space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.getName());
    }

    @Test
    public void testResolveAttachmentReference() throws Exception
    {
        String DEFAULT_WIKI = "xwiki";
        String DEFAULT_SPACE = "XWiki";
        String DEFAULT_PAGE = "WebHome";

        EntityReference reference = resolver.resolve("wiki:space.page@filename.ext", EntityType.ATTACHMENT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("filename.ext", reference.getName());

        reference = resolver.resolve("", EntityType.ATTACHMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_ATTACHMENT, reference.getName());

        reference = resolver.resolve("wiki:space.page@my.png", EntityType.ATTACHMENT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("my.png", reference.getName());

        reference = resolver.resolve("some:file.name", EntityType.ATTACHMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("some:file.name", reference.getName());

        // Test escapes

        reference = resolver.resolve(":.\\@", EntityType.ATTACHMENT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(":.@", reference.getName());
    }

    /**
     * Tests resolving object references.
     */
    @Test
    public void testResolveObjectReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page^xwiki.class[0]", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.getName());

        // default values
        reference = resolver.resolve("", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.getName());

        // without some of the parents
        reference = resolver.resolve("space.page^XWiki.Class[0]", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class[0]", reference.getName());

        reference = resolver.resolve("page^XWiki.Class[0]", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class[0]", reference.getName());

        reference = resolver.resolve("XWiki.Class", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class", reference.getName());

        // property without object
        reference = resolver.resolve("wiki:space.page#property", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("wiki:space.page#property", reference.getName());

        // object with no name
        reference = resolver.resolve("wiki:space.page^", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.getName());

        // test separator escape
        reference = resolver.resolve("wiki:space.page^obje\\^ct", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("obje^ct", reference.getName());
        
        // and that separators don't need to be escaped other than in the object name
        reference = resolver.resolve("wiki:space.page^xwiki.class[0]", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.getName());
        
        reference = resolver.resolve("wiki:space.page^xwi\\\\.ki.class[0]", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwi\\.ki.class[0]", reference.getName());

        reference = resolver.resolve("wiki:spa^ce.page^xwiki.class[0]", EntityType.OBJECT);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("spa^ce", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.getName());

        reference = resolver.resolve(":.\\^@", EntityType.OBJECT);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(":.^@", reference.getName());
    }

    /**
     * Tests resolving object references.
     */
    @Test
    public void testResolvePropertyReference()
    {
        EntityReference reference = resolver.resolve("wiki:space.page^xwiki.class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        // default values
        reference = resolver.resolve("", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals(DEFAULT_OBJECT_PROPERTY, reference.getName());

        // without some of the parents
        reference = resolver.resolve("space.page^XWiki.Class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        reference = resolver.resolve("page^XWiki.Class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        reference = resolver.resolve("XWiki.Class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        reference = resolver.resolve("FooBar", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("FooBar", reference.getName());

        // object without property, parsed as property
        reference = resolver.resolve("wiki:space.page^XWiki.Class[0]", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("wiki:space.page^XWiki.Class[0]", reference.getName());

        // empty prop
        reference = resolver.resolve("wiki:space.page^XWiki.Class#", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("XWiki.Class", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals(DEFAULT_OBJECT_PROPERTY, reference.getName());

        // test separator escape
        reference = resolver.resolve("wiki:space.page^xwiki.class[0]#prop\\#erty", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop#erty", reference.getName());

        // and that separators don't need to be escaped other than in the property name
        reference = resolver.resolve("wiki:space.page^x#wiki.class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("page", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("x#wiki.class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        reference = resolver.resolve("wiki:space.pa#ge^xwiki.class[0]#prop", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals("wiki", reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals("space", reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals("pa#ge", reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals("xwiki.class[0]", reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals("prop", reference.getName());

        reference = resolver.resolve(":^\\#.@", EntityType.OBJECT_PROPERTY);
        Assert.assertEquals(DEFAULT_WIKI, reference.extractReference(EntityType.WIKI).getName());
        Assert.assertEquals(DEFAULT_SPACE, reference.extractReference(EntityType.SPACE).getName());
        Assert.assertEquals(DEFAULT_PAGE, reference.extractReference(EntityType.DOCUMENT).getName());
        Assert.assertEquals(DEFAULT_OBJECT, reference.extractReference(EntityType.OBJECT).getName());
        Assert.assertEquals(":^#.@", reference.getName());
    }
}
