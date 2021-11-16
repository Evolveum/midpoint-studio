package com.evolveum.midpoint.studio.action.task;

import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.action.transfer.RefreshAction;
import com.evolveum.midpoint.studio.client.MidPointObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTask extends BackgroundableTask<TaskState> {

    public static String TITLE = "Upgrade task";

    public static final String NOTIFICATION_KEY = "Upgrade task";

    private static final Logger LOG = Logger.getInstance(TaskUpgradeTask.class);

    public TaskUpgradeTask(AnActionEvent event) {
        super(event.getProject(), TITLE, NOTIFICATION_KEY);

        setEvent(event);
    }

    protected void processEditorText(ProgressIndicator indicator, Editor editor, String text, VirtualFile sourceFile) {
        try {
            String updated = transformTask(text);

            updateEditor(editor, updated);
        } catch (Exception ex) {
            // todo fix
            ex.printStackTrace();
        }
    }

    protected void processFile(VirtualFile file) {
        List<MidPointObject> objects = loadObjectsFromFile(file);

        if (objects.isEmpty()) {
            state.incrementSkipped();
            midPointService.printToConsole(null, RefreshAction.class, "Skipped file " + file.getPath() + " no objects found (parsed).");
            return;
        }

        List<String> newObjects = new ArrayList<>();

        for (MidPointObject object : objects) {
            ProgressManager.checkCanceled();

            if (!ObjectTypes.TASK.equals(object.getType())) {
                newObjects.add(object.getContent());
                state.incrementSkipped();
            }

            try {
                String newContent = transformTask(object.getContent());
                newObjects.add(newContent);

                state.incrementProcessed();
            } catch (Exception ex) {
                state.incrementFailed();
                newObjects.add(object.getContent());

                midPointService.printToConsole(null, RefreshAction.class, "Error upgrading task"
                        + object.getName() + "(" + object.getOid() + ")", ex);
            }
        }

        writeObjectsToFile(file, newObjects);
    }

    private String transformTask(String xml) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        try (InputStream xsltStream = TaskUpgradeTask.class.getClassLoader().getResourceAsStream("task-transformation.xslt")) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.normalize();

            StreamSource xsl = new StreamSource(xsltStream);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer(xsl);
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setParameter(OutputKeys.INDENT, "yes");
            trans.setParameter(OutputKeys.ENCODING, "utf-8");

            StringWriter sw = new StringWriter();

            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();
        }
    }
}
