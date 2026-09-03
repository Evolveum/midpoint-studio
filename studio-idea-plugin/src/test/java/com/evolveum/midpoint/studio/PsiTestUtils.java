package com.evolveum.midpoint.studio;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;


/**
 * Created by Dominik.
 */
public class PsiTestUtils {

    public static PsiFile createPsiFileFromSnippet(Project project, String filename, String text, Language language) {

        LanguageFileType fileType = language.getAssociatedFileType();

        if (fileType == null) {
            throw new IllegalArgumentException("Language " + language.getID() + " has no associated file type");
        }

        return ApplicationManager.getApplication().runReadAction((Computable<PsiFile>) () ->
                PsiFileFactory.getInstance(project).createFileFromText(
                        filename,
                        fileType,
                        text
                )
        );
    }
}
