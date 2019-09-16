package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismSerializer;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.evolveum.midpoint.studio.util.MidPointUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
@State(
        name = "FileObjectManager", storages = @Storage(value = "midpoint.xml")
)
public class FileObjectManagerImpl extends ManagerBase<FileObjectSettings> implements FileObjectManager {

    private static final Logger LOG = Logger.getInstance(FileObjectManagerImpl.class);

    private static final String SCRATCH = "scratch";

    private static final String OBJECTS_XML_PREFIX = "<objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\">\n";

    private static final String OBJECTS_XML_SUFFIX = "</objects>\n";

    public FileObjectManagerImpl(@NotNull Project project) {
        super(project, FileObjectSettings.class);
    }

    @Override
    protected FileObjectSettings createDefaultSettings() {
        return FileObjectSettings.createDefaultSettings();
    }

    @Override
    public <O extends ObjectType> VirtualFile saveObject(PrismObject<O> object, boolean asScratch) {
        return saveObjects(Arrays.asList(object), asScratch)[0];
    }

    @Override
    public <O extends ObjectType> VirtualFile[] saveObjects(List<PrismObject<O>> objects, boolean asScratch) {
        RestObjectManager restObjectManager = RestObjectManager.getInstance(getProject());
        Environment env = restObjectManager.getEnvironment();

        Map<String, String> params = new HashMap<>();
        params.put("s", env.getShortName());    // environment short name
        params.put("e", env.getName());    // environment name

        PrismContext ctx = restObjectManager.getPrismContext();
        PrismSerializer<String> serializer = ctx.serializerFor(PrismContext.LANG_XML);

        FileObjectSettings settings = getSettings();
        if (!asScratch) {
            return createDownloadFileContent(objects, params, settings.getDowloadFilePattern(), serializer);
        }

        return createScratchFileContent(objects, params, settings.getGeneratedFilePattern(), serializer);
    }

    private <O extends ObjectType> VirtualFile[] createScratchFileContent(
            List<PrismObject<O>> objects, Map<String, String> params, String filePattern, PrismSerializer<String> serializer) {
        BufferedWriter out = null;
        try {
            VirtualFile scratchFile = createFile(params, null, filePattern);

            out = new BufferedWriter(
                    new OutputStreamWriter(scratchFile.getOutputStream(this), StandardCharsets.UTF_8));
            out.write(OBJECTS_XML_PREFIX);

            for (PrismObject<O> object : objects) {
                String xml = serializer.serialize(object.getValue(), object.getElementName().asSingleName());
                IOUtils.write(xml, out);
            }

            out.write(OBJECTS_XML_SUFFIX);

            return new VirtualFile[]{scratchFile};
        } catch (Exception ex) {
            ex.printStackTrace();
            // todo error handling
        } finally {
            IOUtils.closeQuietly(out);
        }

        return new VirtualFile[0];
    }

    private <O extends ObjectType> VirtualFile[] createDownloadFileContent(
            List<PrismObject<O>> objects, Map<String, String> params, String filePattern, PrismSerializer<String> serializer) {

        List<VirtualFile> files = new ArrayList<>();

        for (PrismObject<O> object : objects) {
            try {
                VirtualFile objectFile = createFile(params, object, filePattern);

                String xml = serializer.serialize(object);
                try (BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(objectFile.getOutputStream(this), StandardCharsets.UTF_8))) {
                    IOUtils.write(xml, out);
                }

                files.add(objectFile);
            } catch (Exception ex) {
                // todo error handling
                ex.printStackTrace();
            }
        }

        return files.toArray(new VirtualFile[files.size()]);
    }

    private <O extends ObjectType> VirtualFile createFile(Map<String, String> params, PrismObject<O> object, String filePattern)
            throws IOException {

        if (object != null) {
            ObjectTypes type = ObjectTypes.getObjectType(object.getCompileTimeClass());
            params.put("t", type.getElementName().getLocalPart()); // singular type, like "user"
            params.put("T", type.getRestType());                   // plural type, like "users"

            String name = MidPointUtils.escapeObjectName(object.getName().getOrig());
            params.put("n", name);   // object name
            params.put("o", object.getOid());    // object oid
        } else {
            params.put("t", ObjectTypes.OBJECT.getElementName().getLocalPart());
            params.put("T", ObjectTypes.OBJECT.getRestType());

            String directoryPath = VfsUtil.getParentDir(filePattern);
            VirtualFile dir = VfsUtil.createDirectories(getProject().getBasePath()
                    + VfsUtilCore.VFS_SEPARATOR_CHAR + directoryPath);

            String name = createScratchFile(dir);
            params.put("n", name);
            params.put("o", name);
        }

        String path = MidPointUtils.replaceFilePath(filePattern, params);

        String fileName = VfsUtil.extractFileName(path);
        String directoryPath = VfsUtil.getParentDir(path);

        VirtualFile dir = VfsUtil.createDirectories(getProject().getBasePath()
                + VfsUtilCore.VFS_SEPARATOR_CHAR + directoryPath);
        return dir.createChildData(this, fileName);
    }

    private String createScratchFile(VirtualFile parent) {
        for (int i = 1; i < 10000; i++) {
            String scratchName = SCRATCH + "_" + i;
            if (parent.findChild(scratchName + ".xml") == null) {
                return scratchName;
            }
        }

        return null;
    }

    @Override
    public <O extends ObjectType> PrismObject<O> loadObject(String path, boolean expand) {
        return null;
    }
}
