package com.evolveum.midpoint.studio.impl.lang;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ValueMetadataType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

import javax.xml.XMLConstants;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MetadataElementDescriptorProvider implements XmlElementDescriptorProvider {

    private static final String METADATA_TAG_NAME = "_metadata";

    @Override
    public @Nullable XmlElementDescriptor getDescriptor(XmlTag tag) {
        Project project = tag.getProject();
        if (project.isDefault()) return null;

        if (!METADATA_TAG_NAME.equals(tag.getLocalName()) || tag.getAttribute("xsi:type") != null) {
            return null;
        }

        XmlElementFactory factory = XmlElementFactory.getInstance(tag.getProject());
        XmlTag fakeTag = factory.createTagFromText(
                "<" + METADATA_TAG_NAME + " xmlns=\"" + SchemaConstantsGenerated.NS_COMMON + "\" " +
                        "xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\" " +
                        "xsi:type=\"" + ValueMetadataType.COMPLEX_TYPE.getLocalPart() + "\">");

        tag = fakeTag;

        return tag.getDescriptor();
    }
}
