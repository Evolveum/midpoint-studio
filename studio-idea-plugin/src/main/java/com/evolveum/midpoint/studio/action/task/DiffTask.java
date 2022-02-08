package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.evolveum.midpoint.studio.impl.xml.DiffObjectType;
import com.evolveum.midpoint.studio.impl.xml.DiffType;
import com.evolveum.midpoint.studio.impl.xml.LocationType;
import com.evolveum.midpoint.studio.impl.xml.ObjectsDiffFactory;
import com.evolveum.midpoint.studio.util.FileUtils;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class DiffTask extends SimpleBackgroundableTask {

    private static final Logger LOG = Logger.getInstance(DiffTask.class);

    public static final String DIFF_XML_PREFIX = "<diffList>";

    public static final String DIFF_XML_SUFFIX = "</diffList>\n";

    public DiffTask(@NotNull AnActionEvent event, String title, String notificationKey) {
        super(event.getProject(), title, notificationKey);

        setEvent(event);
    }

    protected String createDiffXml(MidPointObject first, VirtualFile firstFile, LocationType firstLocation,
                                   MidPointObject second, VirtualFile secondFile, LocationType secondLocation) throws SchemaException, IOException {
        // todo expand local content before its used for comparing

        PrismObject firstObject = client.parseObject(first.getContent());
        PrismObject secondObject = client.parseObject(second.getContent());

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

    private DiffObjectType createDiffObject(VirtualFile file, LocationType location, PrismObject prismObject) {
        DiffObjectType object = new DiffObjectType();
        if (file != null) {
            object.setFileName(file.getName());
        }
        object.setLocation(location);
        object.setObject((com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType) prismObject.asObjectable());

        return object;
    }
}
