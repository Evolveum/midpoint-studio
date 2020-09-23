package com.evolveum.midpoint.studio.util;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.Environment;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.evolveum.midpoint.studio.impl.MidPointSettings;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FileUtils {

    private static final String SCRATCH = "scratch";

    public static VirtualFile createScratchFile(Project project, Environment env) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("s", env.getShortName());    // environment short name
        params.put("e", env.getName());    // environment name

        MidPointService mm = MidPointService.getInstance(project);
        MidPointSettings settings = mm.getSettings();

        return createFile(project, params, null, null, null, settings.getGeneratedFilePattern());
    }

    public static <O extends ObjectType> VirtualFile createFile(Project project, Environment env,
                                                                Class<O> objectType, String oid, String objectName) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("s", env.getShortName());    // environment short name
        params.put("e", env.getName());    // environment name

        MidPointService mm = MidPointService.getInstance(project);
        MidPointSettings settings = mm.getSettings();

        return createFile(project, params, objectType, oid, objectName, settings.getDowloadFilePattern());
    }

    private static <O extends ObjectType> VirtualFile createFile(Project project, Map<String, String> params,
                                                                 Class<O> objectType, String oid, String objectName, String filePattern)
            throws IOException {

        if (objectType != null) {
            ObjectTypes type = ObjectTypes.getObjectType(objectType);
            params.put("t", type.getElementName().getLocalPart()); // singular type, like "user"
            params.put("T", type.getRestType());                   // plural type, like "users"

            String name = MidPointUtils.escapeObjectName(objectName);
            params.put("n", name);   // object name
            params.put("o", oid);    // object oid
        } else {
            params.put("t", ObjectTypes.OBJECT.getElementName().getLocalPart());
            params.put("T", ObjectTypes.OBJECT.getRestType());

            String directoryPath = VfsUtil.getParentDir(filePattern);
            VirtualFile dir = VfsUtil.createDirectories(project.getBasePath()
                    + VfsUtilCore.VFS_SEPARATOR_CHAR + directoryPath);

            String name = createScratchFile(dir);
            params.put("n", name);
            params.put("o", name);
        }

        String path = MidPointUtils.replaceFilePath(filePattern, params);

        String fileName = VfsUtil.extractFileName(path);
        String directoryPath = VfsUtil.getParentDir(path);

        VirtualFile dir = VfsUtil.createDirectories(project.getBasePath()
                + VfsUtilCore.VFS_SEPARATOR_CHAR + directoryPath);
        return dir.createChildData(project, fileName);
    }

    private static String createScratchFile(VirtualFile parent) {
        for (int i = 1; i < 10000; i++) {
            String scratchName = SCRATCH + "_" + i;
            if (parent.findChild(scratchName + ".xml") == null) {
                return scratchName;
            }
        }

        return null;
    }
}
