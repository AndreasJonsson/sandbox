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
package org.xwiki.rendering.internal.transformation.wikiword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.DocumentLink;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Automatically replace words representing Wiki Words with a link.
 *
 * @version $Id$
 */
@Component("wikiword")
public class WikiWordTransformation extends AbstractTransformation
{
    private static final Pattern WIKIWORD_PATTERN = Pattern.compile("[A-Z][a-z]*([A-Z][a-z]*)+");

    public void transform(Block block, TransformationContext transformationContext) throws TransformationException
    {
        // Find all Word blocks and for each of them check if they're a wiki word or not
        for (WordBlock wordBlock : block.getChildrenByType(WordBlock.class, true)) {
            Matcher matcher = WIKIWORD_PATTERN.matcher(wordBlock.getWord());
            if (matcher.matches()) {
                Link link = new DocumentLink();
                link.setReference(wordBlock.getWord());
                wordBlock.getParent().replaceChild(new LinkBlock(wordBlock.getChildren(), link, false), wordBlock);
            }
        }
    }
}
