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
package org.xwiki.it.ui;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.it.ui.elements.AdministrationPage;
import org.xwiki.it.ui.elements.BasePage;
import org.xwiki.it.ui.elements.HistoryPane;
import org.xwiki.it.ui.elements.ImportPage;
import org.xwiki.it.ui.framework.AbstractAdminAuthenticatedTest;
import org.xwiki.it.ui.framework.TestUtils;

/**
 * Test the Import XAR feature.
 *
 * @version $Id$
 * @since 2.3M1
 */
public class ImportTest extends AbstractAdminAuthenticatedTest
{
    private static final String PACKAGE_WITHOUT_HISTORY = "Main.TestPage-no-history.xar";

    private static final String PACKAGE_WITH_HISTORY = "Main.TestPage-with-history.xar";

    private AdministrationPage adminPage;

    private ImportPage importPage;

    @Before
    public void setUp()
    {
        super.setUp();

        // Delete Test Page we import from XAR to ensure to start with a predefined state.
        TestUtils.deletePage("Main", "TestPage", getDriver());
        
        adminPage = new AdministrationPage(getDriver());
        adminPage.gotoAdministrationPage();

        importPage = adminPage.clickImportSection();

        // Remove our packages if they're there already, to ensure to start with a predefined state.
        if (importPage.isPackagePresent(PACKAGE_WITH_HISTORY)) {
            importPage.deletePackage(PACKAGE_WITH_HISTORY);
            // TODO: Remove this when the delete doesn't redirect to the Admin home page any more (which is a bug)
            importPage = adminPage.clickImportSection();
        }
        if (importPage.isPackagePresent(PACKAGE_WITHOUT_HISTORY)) {
            importPage.deletePackage(PACKAGE_WITHOUT_HISTORY);
            // TODO: Remove this when the delete doesn't redirect to the Admin home page any more (which is a bug)
            importPage = adminPage.clickImportSection();
        }
    }

    @Test
    public void testImportWithHistory() throws IOException
    {
        URL fileUrl = this.getClass().getResource("/" + PACKAGE_WITH_HISTORY);
        
        importPage.attachPackage(fileUrl);
        importPage.selectPackage(PACKAGE_WITH_HISTORY);
        
        importPage.selectReplaceHistoryOption();
        importPage.importPackage();

        BasePage importedPage = importPage.clickImportedPage("Main.TestPage");

        HistoryPane history = importedPage.openHistoryDocExtraPane();

        Assert.assertEquals("4.1", history.getCurrentVersion());
        Assert.assertEquals("Imported from XAR", history.getCurrentVersionComment());
        Assert.assertTrue(history.hasVersionWithSummary("A new version of the document"));
    }
    
    @Test
    public void testImportWithNewHistoryVersion() throws IOException
    {
        URL fileUrl = this.getClass().getResource("/" + PACKAGE_WITHOUT_HISTORY);

        importPage.attachPackage(fileUrl);
        importPage.selectPackage(PACKAGE_WITHOUT_HISTORY);

        importPage.importPackage();

        BasePage importedPage = importPage.clickImportedPage("Main.TestPage");

        HistoryPane history = importedPage.openHistoryDocExtraPane();

        Assert.assertEquals("1.1", history.getCurrentVersion());
        Assert.assertEquals("Imported from XAR", history.getCurrentVersionComment());
    }
}
