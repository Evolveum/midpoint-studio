package com.evolveum.midpoint.studio.impl.cache;

import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.studio.impl.MidPointService;
import com.intellij.javaee.ExternalResourceManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * todo improve cleanup mechanism. Check ExternalSystemManager or classes around to listen for "maven" changes
 * <p>
 * Created by Viliam Repan (lazyman).
 */
public class XmlSchemaCacheService {

    private static final Logger LOG = Logger.getInstance(XmlSchemaCacheService.class);

    private static final Long CACHE_MAX_TTL = 60 * 1000L; // 1 minute

    private final Set<String> UNKNOWN = ConcurrentHashMap.newKeySet();

    private final ConcurrentMap<String, XmlFile> SCHEMAS = ContainerUtil.createConcurrentSoftKeySoftValueMap();

    private long lastRefresh = System.currentTimeMillis();

    public synchronized XmlFile getSchema(String url, PsiFile baseFile) {
        if (url == null) {
            return null;
        }

        if (lastRefresh + CACHE_MAX_TTL < System.currentTimeMillis()) {
            LOG.info("Clearing XML schemas cache");

            UNKNOWN.clear();
            SCHEMAS.clear();

            lastRefresh = System.currentTimeMillis();
        }

        if (UNKNOWN.contains(url)) {
            return null;
        }

        XmlFile schema = SCHEMAS.get(url);
        if (schema != null) {
            return schema;
        }

        if (!url.startsWith("http://midpoint.evolveum.com")
                && !url.startsWith("http://prism.evolveum.com")) {
            return null;
        }

        if (SchemaConstantsGenerated.NS_ICF_CONFIGURATION.equals(url)) {
            // this url is handled via ConnectorXmlSchemaCacheService
            return null;
        }

        String resourceUrl = url.replaceFirst("http://midpoint.evolveum.com", "");
        resourceUrl = resourceUrl.replaceFirst("http://prism.evolveum.com", "");

        resourceUrl += ".xsd";

        Path path = Path.of(resourceUrl);   // e.g. /xml/ns/public/common/common-core-3.xsd
        Path dir = path.getParent();
        String file = path.getFileName().toString();

        String package_ = dir.toString().replace(File.separator, ".");
        if (package_.startsWith(".")) {
            package_ = package_.replaceFirst("\\.", "");
        }

        JavaPsiFacade jpf = JavaPsiFacade.getInstance(baseFile.getProject());
        PsiPackage psiPackage = jpf.findPackage(package_);
        if (psiPackage == null) {
            UNKNOWN.add(url);
            return null;
        }


        PsiFile[] files = psiPackage.getFiles(psiPackage.getResolveScope());
        if (files == null) {
            UNKNOWN.add(url);
            return null;
        }

        for (PsiFile f : files) {
            if (f.getName().equals(file)) {
                schema = (XmlFile) f;
                break;
            }
        }

        if (schema == null) {
            LOG.trace("Couldn't find schema for url '" + url + "', tried '" + resourceUrl + "'");
            UNKNOWN.add(url);

            return null;
        }

        SCHEMAS.put(url, schema);

        checkUserDefinedXsdSchemas(baseFile.getProject(), url);

        return schema;
    }

    private void checkUserDefinedXsdSchemas(Project project, String url) {
        ExternalResourceManagerEx manager = ExternalResourceManagerEx.getInstanceEx();

        String[] urls = manager.getAvailableUrls();
        if (urls == null) {
            return;
        }

        boolean found = Arrays.stream(urls).anyMatch(u -> Objects.equals(u, url));
        if (!found) {
            return;
        }

        UIUtil.invokeLaterIfNeeded(() -> {
            MidPointService mpService = project.getService(MidPointService.class);
            mpService.printToConsole(null, XmlSchemaCacheService.class, "Warning: Namespace URL '" + url + "' also defined as custom user resource. This can possible interfere with Midpoint XSD files linked by Midpoint Studio plugin.");
        });
    }

    public synchronized void clear() {
        UNKNOWN.clear();
        SCHEMAS.clear();
    }
}
