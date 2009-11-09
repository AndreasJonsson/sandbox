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
 *
 */
package org.xwiki.eclipse.ui.dialogs;

import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xwiki.eclipse.core.XWikiEclipseException;
import org.xwiki.eclipse.core.model.XWikiEclipseAttachmentSummary;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnableWithResult;

public class RenameAttachmentDialog extends TitleAreaDialog
{
    private XWikiEclipseAttachmentSummary attachmentSummary;

    private String toSpace;

    private String toPageName;

    private String tofileName;

    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Rename/Move Attachment");
    }

    @Override
    protected Point getInitialSize()
    {
        return new Point(800, 600);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        Button button = createButton(parent, IDialogConstants.OK_ID, "Rename", true);
        button.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
            }
        });

        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);

        setTitle("Rename/Move Attachment");

        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite mainComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().applyTo(mainComposite);

        Composite composite = new Composite(mainComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText("New space:");

        final ComboViewer comboViewer = new ComboViewer(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(comboViewer.getControl());
        comboViewer.setContentProvider(new IStructuredContentProvider()
        {
            public Object[] getElements(Object inputElement)
            {
                XWikiEclipseSafeRunnableWithResult<List<XWikiEclipseSpaceSummary>> runnable =
                    new XWikiEclipseSafeRunnableWithResult<List<XWikiEclipseSpaceSummary>>()
                    {

                        public void run() throws Exception
                        {
                            setResult(attachmentSummary.getDataManager().getSpaces());
                        }

                    };
                SafeRunner.run(runnable);

                if (runnable.getResult() != null) {
                    String[] elements = new String[runnable.getResult().size()];
                    int i = 0;
                    for (XWikiEclipseSpaceSummary spaceSummary : runnable.getResult()) {
                        elements[i] = spaceSummary.getData().getKey();
                        i++;
                    }

                    return elements;
                }

                return new Object[0];
            }

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }
        });
        comboViewer.setLabelProvider(new LabelProvider());
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    toSpace = (String) selection.getFirstElement();
                }
            }
        });
        comboViewer.getCombo().addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                toSpace = comboViewer.getCombo().getText();
            }

        });

        comboViewer.setInput(new Object());
        try {
            comboViewer.setSelection(new StructuredSelection(attachmentSummary.getDataManager().getPage(
                attachmentSummary.getData().getPageId()).getData().getSpace()));
        } catch (XWikiEclipseException e1) {
            e1.printStackTrace();
        }

        label = new Label(composite, SWT.NONE);
        label.setText("New Page:");

        final ComboViewer comboViewer2 = new ComboViewer(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(comboViewer2.getControl());
        comboViewer2.setContentProvider(new IStructuredContentProvider()
        {
            public Object[] getElements(Object inputElement)
            {
                XWikiEclipseSafeRunnableWithResult<List<XWikiEclipsePageSummary>> runnable =
                    new XWikiEclipseSafeRunnableWithResult<List<XWikiEclipsePageSummary>>()
                    {

                        public void run() throws Exception
                        {
                            setResult(attachmentSummary.getDataManager().getPages(toSpace));
                        }

                    };
                SafeRunner.run(runnable);

                if (runnable.getResult() != null) {
                    String[] elements = new String[runnable.getResult().size()];
                    int i = 0;
                    for (XWikiEclipsePageSummary pageSummary : runnable.getResult()) {
                        elements[i] = pageSummary.getData().getId().split("\\.")[1];
                        i++;
                    }

                    return elements;
                }

                return new Object[0];
            }

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }
        });

        comboViewer2.setLabelProvider(new LabelProvider());
        comboViewer2.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    toPageName = (String) selection.getFirstElement();
                }
            }
        });
        comboViewer2.getCombo().addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                toPageName = comboViewer2.getCombo().getText();
            }

        });

        comboViewer2.setInput(new Object());
        comboViewer2.setSelection(new StructuredSelection(attachmentSummary.getPageSummary().getId().split("\\.")[1]));

        comboViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (selection != null) {
                    toSpace = (String) selection.getFirstElement();
                }
                comboViewer2.refresh();
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText("New Attachment Name:");

        final Text name = new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(name);

        name.setText(attachmentSummary.getData().getFileName());

        name.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                tofileName = name.getText();
            }

        });

        return composite;
    }

    public String getToSpace()
    {
        return toSpace;
    }

    public String getToPageName()
    {
        return toPageName;
    }

    public String getToFileName()
    {
        return tofileName;
    }

    public RenameAttachmentDialog(Shell parentShell)
    {
        super(parentShell);
    }

    public RenameAttachmentDialog(Shell activeShell, XWikiEclipseAttachmentSummary attachmentSummary)
    {
        super(activeShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.attachmentSummary = attachmentSummary;
    }

}
