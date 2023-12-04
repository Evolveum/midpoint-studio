package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ExtractLocalizationPropertiesAction extends AnAction {

    private static final String NS_JAXB = "http://java.sun.com/xml/ns/jaxb";

    private static final QName XSD_SCHEMA = new QName(W3C_XML_SCHEMA_NS_URI, "schema");

    private static final QName XSD_COMPLEX_TYPE = new QName(W3C_XML_SCHEMA_NS_URI, "complexType");

    private static final QName XSD_SIMPLE_TYPE = new QName(W3C_XML_SCHEMA_NS_URI, "simpleType");

    private static final QName XSD_ELEMENT = new QName(W3C_XML_SCHEMA_NS_URI, "element");

    private static final QName XSD_SEQUENCE = new QName(W3C_XML_SCHEMA_NS_URI, "sequence");

    private static final QName XSD_RESTRICTION = new QName(W3C_XML_SCHEMA_NS_URI, "restriction");

    private static final QName XSD_COMPLEX_CONTENT = new QName(W3C_XML_SCHEMA_NS_URI, "complexContent");

    private static final QName XSD_APPINFO = new QName(W3C_XML_SCHEMA_NS_URI, "appinfo");

    private static final QName XSD_ANNOTATION = new QName(W3C_XML_SCHEMA_NS_URI, "annotation");

    private static final QName XSD_EXTENSION = new QName(W3C_XML_SCHEMA_NS_URI, "extension");

    private static final QName XSD_ENUMERATION = new QName(W3C_XML_SCHEMA_NS_URI, "enumeration");

    private static final QName XSD_CHOICE = new QName(W3C_XML_SCHEMA_NS_URI, "choice");

    private static final QName JAXB_TYPESAFE_ENUM_MEMBER = new QName(NS_JAXB, "typesafeEnumMember");

    private static final String ATTRIBUTE_NAME = "name";

    private static final String ATTRIBUTE_VALUE = "value";

    @Override
    public void update(@NotNull AnActionEvent evt) {
        super.update(evt);

        List<VirtualFile> xsdFiles = getSelectedXsdFiles(evt);
        evt.getPresentation().setVisible(!xsdFiles.isEmpty());
    }

    private List<VirtualFile> getSelectedXsdFiles(AnActionEvent evt) {
        VirtualFile[] selectedFiles = ApplicationManager.getApplication().runReadAction(
                (Computable<VirtualFile[]>) () -> evt.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY));

        return MidPointUtils.filterXsdFiles(selectedFiles);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent evt) {
        Project project = evt.getProject();

        if (project == null) {
            return;
        }

        Map<String, String> existing = loadProps(project);

        PsiElement element = evt.getData(LangDataKeys.PSI_ELEMENT);
        if (element instanceof XmlTag) {
            Map<String, String> properties = new HashMap<>();
            processElement((XmlTag) element, properties);
            dumpToConsole(project, properties, existing);
            return;
        }

        List<VirtualFile> files = getSelectedXsdFiles(evt);

        Map<String, String> properties = new HashMap<>();
        for (VirtualFile file : files) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (!(psiFile instanceof XmlFile)) {
                continue;
            }

            XmlFile xmlFile = (XmlFile) psiFile;
            XmlTag root = xmlFile.getRootTag();
            if (!XSD_SCHEMA.equals(MidPointUtils.createQName(root))) {
                continue;
            }

            for (XmlTag tag : root.getSubTags()) {
                processElement(tag, properties);
            }
        }

        dumpToConsole(project, properties, existing);
    }

    private Map<String, String> loadProps(Project project) {
        FileChooserDialog dialog = FileChooserFactory.getInstance()
                .createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor("properties"), project, null);

        VirtualFile[] files = dialog.choose(project);

        if (files == null || files.length == 0) {
            return new HashMap<>();
        }

        VirtualFile existingProperties = files[0];

        Properties properties = new Properties();
        try (InputStream is = existingProperties.getInputStream()) {
            properties.load(is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Map<String, String> map = new HashMap<>();
        map.putAll((Map) properties);

        return map;
    }

    private void processElement(XmlTag tag, Map<String, String> properties) {
        QName tagType = MidPointUtils.createQName(tag);
        if (XSD_ELEMENT.equals(tagType)) {
            return;
        }

        String typeName = tag.getAttributeValue(ATTRIBUTE_NAME);
        if (typeName == null) {
            return;
        }

        if (typeName.contains(":")) {
            typeName = typeName.substring(typeName.indexOf(":") + 1);
        }

        if (XSD_COMPLEX_TYPE.equals(tagType)) {
            processComplexType(properties, tag, typeName);
        } else if (XSD_SIMPLE_TYPE.equals(tagType)) {
            processSimpleType(properties, tag, typeName);
        }
    }

    private void processSimpleType(Map<String, String> properties, XmlTag tag, String typeName) {
        XmlTag restriction = MidPointUtils.findSubTag(tag, XSD_RESTRICTION);
        if (restriction == null) {
            return;
        }

        getProperties(restriction, properties, typeName);
    }

    private void processComplexType(Map<String, String> properties, XmlTag tag, String typeName) {
        XmlTag sequence = MidPointUtils.findSubTag(tag, XSD_SEQUENCE);
        if (sequence == null) {
            XmlTag complexContent = MidPointUtils.findSubTag(tag, XSD_COMPLEX_CONTENT);
            if (complexContent == null) {
                return;
            }

            XmlTag extension = MidPointUtils.findSubTag(complexContent, XSD_EXTENSION);
            if (extension == null) {
                return;
            }

            sequence = MidPointUtils.findSubTag(extension, XSD_SEQUENCE);
        }

        if (sequence == null) {
            return;
        }

        getProperties(sequence, properties, typeName);
    }

    private void dumpToConsole(Project project, Map<String, String> properties, Map<String, String> existing) {
        MidPointService ms = MidPointService.getInstance(project);

        StringBuilder sb = new StringBuilder();
        sb.append("Generated localization:\n");

        List<String> keys = new ArrayList<>();
        properties.keySet().forEach(k -> {
            if (existing != null && !existing.containsKey(k)) {
                keys.add(k);
            }
        });

        Collections.sort(keys);

        keys.forEach(k -> sb.append(k + "=" + properties.get(k)).append('\n'));

        ms.printToConsole(null, TestAction.class, sb.toString());
    }

    private void getProperties(XmlTag tag, Map<String, String> properties, String keyPrefix) {
        QName tagName = MidPointUtils.createQName(tag);

        if (XSD_SEQUENCE.equals(tagName) || XSD_CHOICE.equals(tagName)) {
            for (XmlTag e : tag.getSubTags()) {
                getProperties(e, properties, keyPrefix);
            }

            return;
        } else if (XSD_RESTRICTION.equals(tagName)) {
            for (XmlTag e : MidPointUtils.findSubTags(tag, XSD_ENUMERATION)) {
                getProperties(e, properties, keyPrefix);
            }

            return;
        }

        String name = tag.getAttributeValue(ATTRIBUTE_NAME);
        if (XSD_ELEMENT.equals(tagName)) {
            // we already have xsd:element[name]
        } else if (XSD_ENUMERATION.equals(tagName)) {
            XmlTag annotation = MidPointUtils.findSubTag(tag, XSD_ANNOTATION);
            if (annotation != null) {
                XmlTag appinfo = MidPointUtils.findSubTag(annotation, XSD_APPINFO);
                if (appinfo != null) {
                    XmlTag typesafeEnumMember = MidPointUtils.findSubTag(appinfo, JAXB_TYPESAFE_ENUM_MEMBER);
                    if (typesafeEnumMember != null && typesafeEnumMember.getAttributeValue(ATTRIBUTE_NAME) != null) {
                        name = typesafeEnumMember.getAttributeValue(ATTRIBUTE_NAME);
                    }
                }
            }

            if (name == null) {
                name = tag.getAttributeValue(ATTRIBUTE_VALUE);
            }
        }

        if (name == null) {
            return;
        }

        String value = StringUtils.capitalize(name.replaceAll("([A-Z][a-z])", " $1").toLowerCase());
        if (XSD_ENUMERATION.equals(tagName)) {
            value = value.replaceAll("_", " ");
        }

        properties.put(keyPrefix + "." + name, value);
    }
}
