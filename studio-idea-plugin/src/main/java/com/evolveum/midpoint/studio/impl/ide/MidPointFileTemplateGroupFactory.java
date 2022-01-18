package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.studio.MidPointIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import icons.OpenapiIcons;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFileTemplateGroupFactory implements FileTemplateGroupDescriptorFactory {

    public static final String MIDPOINT_MAVEN_POM_TEMPLATE = "MidPoint Maven Project.xml";

    public static final String MIDPOINT_GIT_IGNORE_TEMPLATE = "MidPoint Git Ignore.git";

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("MidPoint", MidPointIcons.Midpoint);

        group.addTemplate(new FileTemplateDescriptor(MIDPOINT_MAVEN_POM_TEMPLATE, OpenapiIcons.RepositoryLibraryLogo));
        group.addTemplate(new FileTemplateDescriptor(MIDPOINT_GIT_IGNORE_TEMPLATE, MidPointIcons.Midpoint));

        return group;
    }
}
