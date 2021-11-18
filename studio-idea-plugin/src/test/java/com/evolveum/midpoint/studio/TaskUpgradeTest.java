package com.evolveum.midpoint.studio;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import org.bouncycastle.util.Arrays;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Iterator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TaskUpgradeTest {

    @Disabled
    @Test
    public void transformTask() {
        try {
//            EnvironmentCheck check = new EnvironmentCheck();
//            check.checkEnvironment(new PrintWriter(System.out));
//
//            System.out.println();

            File parent = new File(".");
            File stylesheet = new File(parent, "src/main/resources/task-transformation.xslt");
            File datafile = new File(parent, "src/test/testData/task-upgrade/ls-1-input.xml");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(datafile);
            doc.normalize();

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {

                @Override
                public String getNamespaceURI(String prefix) {
                    return "c".equals(prefix) ? SchemaConstantsGenerated.NS_COMMON : null;
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return SchemaConstantsGenerated.NS_COMMON.equals(namespaceURI) ? "c" : null;
                }

                @Override
                public Iterator<String> getPrefixes(String namespaceURI) {
                    return SchemaConstantsGenerated.NS_COMMON.equals(namespaceURI) ? new Arrays.Iterator(new String[]{"c"}) : null;
                }
            });

            //Get first match
            Boolean exists = (Boolean) xpath.evaluate("/c:task/c:activity", doc, XPathConstants.BOOLEAN);
            if (exists) {
                System.out.println("unchanged");
                return;
            }

            StreamSource xsl = new StreamSource(stylesheet);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer(xsl);
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setParameter(OutputKeys.INDENT, "yes");
            trans.setParameter(OutputKeys.ENCODING, "utf-8");

            trans.transform(new DOMSource(doc), new StreamResult(System.out));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
