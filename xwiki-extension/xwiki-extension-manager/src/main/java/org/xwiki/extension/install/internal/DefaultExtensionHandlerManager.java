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
package org.xwiki.extension.install.internal;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.install.ExtensionHandler;
import org.xwiki.extension.install.ExtensionHandlerManager;

@Component
public class DefaultExtensionHandlerManager implements ExtensionHandlerManager
{
    @Requirement
    private ComponentManager componentManager;

    private ExtensionHandler getExtensionHandler(LocalExtension localExtension) throws ComponentLookupException
    {
        // Load extension
        return this.componentManager.lookup(ExtensionHandler.class, localExtension.getType().toString().toLowerCase());
    }

    public void install(LocalExtension localExtension) throws InstallException
    {
        try {
            // Load extension
            ExtensionHandler extensionHandler = getExtensionHandler(localExtension);

            extensionHandler.install(localExtension);
        } catch (ComponentLookupException e) {
            throw new InstallException("Can't find any extension installer for the extension type [" + localExtension
                + "]");
        }
    }

    public void uninstall(LocalExtension localExtension) throws UninstallException
    {
        try {
            // Load extension
            ExtensionHandler extensionHandler = getExtensionHandler(localExtension);

            extensionHandler.uninstall(localExtension);
        } catch (ComponentLookupException e) {
            throw new UninstallException("Can't find any extension installer for the extension type [" + localExtension
                + "]");
        }
    }

    public void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension)
        throws InstallException
    {
        try {
            // Load extension
            ExtensionHandler extensionInstaller = getExtensionHandler(previousLocalExtension);

            extensionInstaller.upgrade(previousLocalExtension, newLocalExtension);
        } catch (ComponentLookupException e) {
            throw new InstallException("Can't find any extension installer for the extension type ["
                + newLocalExtension + "]");
        }
    }
}
