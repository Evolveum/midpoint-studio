package com.evolveum.midpoint.studio.action;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.impl.*;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.intellij.diff.actions.CompareFilesAction;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompareFileWithServerAction extends CompareFilesAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setText("Compare with MidPoint Server");
    }

    @Override
    protected boolean isAvailable(@NotNull AnActionEvent e) {
        DiffRequest request = e.getData(DIFF_REQUEST);
        if (request != null) {
            return true;
        }

        EnvironmentService em = EnvironmentService.getInstance(e.getProject());
        if (!em.isEnvironmentSelected()) {
            return false;
        }

        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        return isAvailable(files, e.getProject());
    }

    private boolean isAvailable(VirtualFile[] files, Project project) {
        if (files == null || files.length == 0) {
            return false;
        }

        for (VirtualFile file : files) {
            if (file.isDirectory()) {
                if (isAvailable(file.getChildren(), project)) {
                    return true;
                } else {
                    continue;
                }
            }

            if (RunnableUtils.executeWithPluginClassloader(() -> isAvailable(file, project))) {
                return true;
            }
        }

        return false;
    }

    private boolean isAvailable(VirtualFile file, Project project) {
        if (file.isDirectory()) {
            return false;
        }

        if (!"xml".equalsIgnoreCase(file.getExtension()) || !hasContent(file)) {
            return false;
        }

        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null || !(psiFile instanceof XmlFile)) {
            return false;
        }

        XmlFile xmlFile = (XmlFile) psiFile;
        if (!SchemaConstantsGenerated.NS_COMMON.equals(xmlFile.getRootTag().getNamespace())) {
            return false;
        }

        List<PrismObject<?>> objects = parseObjects(project, file);
        for (PrismObject object : objects) {
            if (object.getOid() != null) {
                return true;
            }
        }

        return false;
    }

    private List<PrismObject<?>> parseObjects(Project project, VirtualFile file) {
        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(project, env);

        List<PrismObject<?>> objects = new ArrayList<>();
        try {
            List list = client.parseObjects(file);

            if (list != null) {
                objects.addAll(list);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return objects;
    }

    @Nullable
    @Override
    protected DiffRequestChain getDiffRequestChain(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        DiffRequest diffRequest = getDiffRequest(e);
        if (diffRequest != null) {
            return new SimpleDiffRequestChain(diffRequest);
        }

        VirtualFile[] data = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        VirtualFile file1 = data[0];
        VirtualFile file2 = getOtherFile(project, file1);

        if (file2 == null || !hasContent(file2)) {
            return null;
        }

        if (!file1.isValid() || !file2.isValid()) return null; // getOtherFile() shows dialog that can invalidate files

        return createMutableChainFromFiles(project, file1, file2);
    }

    private VirtualFile getOtherFile(Project project, VirtualFile file1) {
        EnvironmentService em = EnvironmentService.getInstance(project);
        Environment env = em.getSelected();
        MidPointClient client = new MidPointClient(project, env);


        PrismObject<? extends ObjectType> obj = null;

        try {
            obj = client.parseObject(file1);
        } catch (Exception ex) {
            // todo handle exception properly
            throw new RuntimeException(ex);
        }
        if (obj == null || StringUtils.isEmpty(obj.getOid())) {
            return null;
        }

        try {
            MidPointObject other = client.get(obj.getCompileTimeClass(), obj.getOid(), new SearchOptions().raw(true));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // todo create virtual in memory file
        return null;

//        PrismContext ctx = client.getPrismContext();
//        QueryFactory qf = ctx.queryFactory();
//
//        InOidFilter filter = qf.createInOid(obj.getOid());
//        ObjectQuery query = qf.createQuery(filter);
//
//        // todo don't download as "scratch", just create virtual in memory file...
//        VirtualFile[] files = restObjectManager.download(obj.getCompileTimeClass(), query,
//                new DownloadOptions().raw(true).showOnly(true));
//
//        return files[0];
    }
}
