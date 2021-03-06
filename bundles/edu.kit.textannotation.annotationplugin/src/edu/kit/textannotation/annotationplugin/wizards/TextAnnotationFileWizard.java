package edu.kit.textannotation.annotationplugin.wizards;

import edu.kit.textannotation.annotationplugin.PluginConfig;
import edu.kit.textannotation.annotationplugin.textmodel.AnnotationSet;
import edu.kit.textannotation.annotationplugin.textmodel.TextModelData;
import edu.kit.textannotation.annotationplugin.textmodel.xmlinterface.TextModelDataXmlInterface;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/**
 * This wizard is responsible for creating new annotatable text files. It is
 * registered as an direct contribution by the eclipse plugin.
 */
public class TextAnnotationFileWizard extends Wizard implements INewWizard {
	private TextAnnotationFileWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for TextAnnotationFileWizard.
	 */
	public TextAnnotationFileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new TextAnnotationFileWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String profileName = page.getProfile();
		final String templateFile = page.getTemplateFileName();
		return this.performCreationTextAnnotationFile(containerName, fileName, profileName, templateFile);
	}
	
	public boolean performCreationTextAnnotationFile(String containerName, String fileName, 
			String profileName, String templateFile) {
		IRunnableWithProgress op = monitor -> {
			try {
				doFinish(containerName, fileName, profileName, templateFile, monitor);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			} finally {
				monitor.done();
			}
		};
		try {
			if(getContainer() != null) {
				getContainer().run(true, false, op);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(
		String containerName,
		String fileName,
		String profileName,
		String templateFile,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(profileName, templateFile);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(() -> {
			IWorkbenchPage page =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, file, true);
			} catch (PartInitException e) {
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with either a template file or empty content.
	 */
	private InputStream openContentStream(String profileName, String templateFile) throws CoreException {
		String content = "";
		String template = "";

		if (templateFile.length() != 0) {
			try {
				template = new String(Files.readAllBytes(Paths.get(templateFile)));
			} catch (IOException e) {
				throwCoreException("Can't read template at " + templateFile);
			}
		}

		content = (new TextModelDataXmlInterface()).buildXml(
				new TextModelData(new AnnotationSet(), profileName, new Document(template)));

		return new ByteArrayInputStream(content.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, PluginConfig.PLUGIN_ID, IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}