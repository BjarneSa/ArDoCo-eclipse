package edu.kit.textannotation.annotationplugin.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This eclipse UI perspective defines the relevant UI components for the text annotation workflow, i.e. the control
 * view on the right, the info view on the left, and specific shortcuts that are defined by the plugin. This
 * perspective is automatically opened if an annotatable text file is opened.
 *
 * @see AnnotationControlsView
 * @see AnnotationInfoView
 */
public class AnnotationPerspective implements IPerspectiveFactory {
    public final static String ID = "edu.kit.textannotation.annotationplugin.views.AnnotationPerspective";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, .25f, editorArea);
        left.addView("org.eclipse.jdt.ui.PackageExplorer");

        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, .75f, editorArea);
        right.addView("edu.kit.textannotation.annotationplugin.views.AnnotationControlsView");

        IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, .75f, editorArea);
        bottom.addView("edu.kit.textannotation.annotationplugin.views.AnnotationInfoView");


        layout.addNewWizardShortcut("edu.kit.textannotation.annotationplugin.wizards.TextAnnotationFileWizard");
        layout.addNewWizardShortcut("edu.kit.textannotation.annotationplugin.wizards.ProfileWizard");

        layout.addShowViewShortcut("edu.kit.textannotation.annotationplugin.views.AnnotationControlsView");
        layout.addShowViewShortcut("edu.kit.textannotation.annotationplugin.views.AnnotationInfoView");
    }
}
