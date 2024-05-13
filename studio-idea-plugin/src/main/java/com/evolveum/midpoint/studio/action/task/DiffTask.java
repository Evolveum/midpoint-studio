package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.ExpanderOptions;
import com.evolveum.midpoint.studio.impl.xml.DiffObjectType;
import com.evolveum.midpoint.studio.impl.xml.DiffType;
import com.evolveum.midpoint.studio.impl.xml.LocationType;
import com.evolveum.midpoint.studio.impl.xml.ObjectsDiffFactory;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class DiffTask extends SimpleBackgroundableTask {

    public static final String DIFF_XML_PREFIX = "<diffList>";

    public static final String DIFF_XML_SUFFIX = "</diffList>\n";

    public DiffTask(
            @NotNull Project project, Supplier<DataContext> dataContextSupplier, String title, String notificationKey) {

        super(project, dataContextSupplier, title, notificationKey);
    }

    protected String createDiffXml(MidPointObject first, VirtualFile firstFile, LocationType firstLocation,
                                   MidPointObject second, VirtualFile secondFile, LocationType secondLocation) throws SchemaException, IOException {

        ExpanderOptions opts = new ExpanderOptions().expandEncrypted(false);

        PrismObject<?> firstObject = client.parseObject(first.getContent(), firstFile, opts);
        PrismObject<?> secondObject = client.parseObject(second.getContent(), opts);

        DiffType objectsDiff = new DiffType();

        DiffObjectType firstDiffObject = createDiffObject(firstFile, firstLocation, firstObject);
        objectsDiff.setFirstObject(firstDiffObject);

        DiffObjectType secondDiffObject = createDiffObject(secondFile, secondLocation, secondObject);
        objectsDiff.setSecondObject(secondDiffObject);

        ObjectsDiffFactory factory = new ObjectsDiffFactory(client.getPrismContext());

        return factory.serializeObjectsDiffToString(objectsDiff);
    }

    protected VirtualFile createScratchAndWriteDiff(List<String> diffs) throws IOException {
        VirtualFile vf = FileUtils.createScratchFile(getProject(), getEnvironment(), "diff");

        try (Writer writer = new OutputStreamWriter(vf.getOutputStream(this), vf.getCharset())) {

            if (diffs.size() > 1) {
                writer.write(DIFF_XML_PREFIX);
                writer.write('\n');
            }

            for (String obj : diffs) {
                writer.write(obj);
            }

            if (diffs.size() > 1) {
                writer.write(DIFF_XML_SUFFIX);
                writer.write('\n');
            }
        }

        return vf;
    }

    private DiffObjectType createDiffObject(VirtualFile file, LocationType location, PrismObject<?> prismObject) {
        DiffObjectType object = new DiffObjectType();
        if (file != null) {
            object.setFileName(file.getName());
        }
        object.setLocation(location);
        object.setObject((com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType) prismObject.asObjectable());

        return object;
    }
}
