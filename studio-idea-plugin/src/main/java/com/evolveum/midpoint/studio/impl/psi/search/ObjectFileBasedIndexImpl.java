package com.evolveum.midpoint.studio.impl.psi.search;

import com.evolveum.midpoint.prism.ParsingContext;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismParser;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.studio.impl.StudioPrismContextService;
import com.evolveum.midpoint.studio.impl.configuration.MidPointService;
import com.evolveum.midpoint.studio.util.RunnableUtils;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ObjectFileBasedIndexImpl extends FileBasedIndexExtension<String, OidNameValue> {

    private static final Logger LOG = Logger.getInstance(ObjectFileBasedIndexImpl.class);

    @NonNls
    public static final ID<String, OidNameValue> NAME = ID.create("MidPointObjectsIndex");

    private final KeyDescriptor<String> KEY_DESCRIPTOR = new KeyDescriptor<>() {

        @Override
        public int getHashCode(final String value) {
            return value.hashCode();
        }

        @Override
        public boolean isEqual(final String val1, final String val2) {
            return val1.equals(val2);
        }

        @Override
        public void save(@NotNull final DataOutput out, final String value) throws IOException {
            out.writeUTF(value);
        }

        @Override
        public String read(@NotNull final DataInput in) throws IOException {
            return in.readUTF();
        }
    };

    private final DataExternalizer<OidNameValue> VALUE_EXTERNALIZER = new KeyDescriptor<>() {

        @Override
        public int getHashCode(OidNameValue value) {
            return value.hashCode();
        }

        @Override
        public boolean isEqual(OidNameValue val1, OidNameValue val2) {
            return val1.equals(val2);
        }

        @Override
        public void save(@NotNull DataOutput out, OidNameValue value) throws IOException {
            out.writeUTF(value.getOid());
            out.writeUTF(value.getName());
            out.writeUTF(value.getType().name());
            out.writeUTF(value.getSource());
        }

        @Override
        public OidNameValue read(@NotNull DataInput in) throws IOException {
            String oid = in.readUTF();
            String name = in.readUTF();
            String type = in.readUTF();
            String source = in.readUTF();

            return new OidNameValue(oid, name, ObjectTypes.valueOf(type), source);
        }
    };

    private final DataIndexer<String, OidNameValue, FileContent> INDEXER = inputData -> {
        LOG.debug("Parsing " + inputData.getFileName());

        Map<String, OidNameValue> map = new HashMap<>();

        Runnable runnable = new RunnableUtils.PluginClasspathRunnable() {

            @Override
            public void runWithPluginClassLoader() {
                PrismContext ctx = StudioPrismContextService.getPrismContext(inputData.getProject());

                ParsingContext parsingContext = ctx.createParsingContextForCompatibilityMode();
                PrismParser parser = ctx.parserFor(new ByteArrayInputStream(inputData.getContent())).language(PrismContext.LANG_XML).context(parsingContext);

                try {
                    List<PrismObject<?>> objects = parser.parseObjects();
                    for (PrismObject o : objects) {
                        if (o.getOid() == null) {
                            continue;
                        }

                        map.put(o.getOid(), new OidNameValue(
                                o.getOid(),
                                o.getName() != null ? o.getName().getOrig() : null,
                                ObjectTypes.getObjectType(o.getCompileTimeClass()),
                                inputData.getFileName()));
                    }
                } catch (Exception ex) {
                    LOG.trace("Couldn't parse file, reason: " + ex.getMessage());
                }
            }
        };

        runnable.run();

        return map;
    };

    @Override
    public @NotNull ID<String, OidNameValue> getName() {
        return NAME;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE) {

            public boolean acceptInput(@NotNull VirtualFile file) {
                if (!"xml".equals(file.getExtension())) {
                    return false;
                }

                return file.isInLocalFileSystem();
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public @NotNull DataIndexer<String, OidNameValue, FileContent> getIndexer() {
        return INDEXER;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return KEY_DESCRIPTOR;
    }

    @Override
    public @NotNull DataExternalizer<OidNameValue> getValueExternalizer() {
        return VALUE_EXTERNALIZER;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    private static GlobalSearchScope createFilter(final Project project, boolean downloadFolderOnly) {
        final GlobalSearchScope projectScope = GlobalSearchScope.allScope(project);

        return new GlobalSearchScope(project) {

            @Override
            public int compare(@NotNull VirtualFile file1, @NotNull VirtualFile file2) {
                return projectScope.compare(file1, file2);
            }

            @Override
            public boolean isSearchInModuleContent(@NotNull Module aModule) {
                return true;
            }

            @Override
            public boolean contains(@NotNull VirtualFile file) {
                String downloadFilePattern = MidPointService.get(project).getSettings().getDowloadFilePattern();
                Path path = Paths.get(downloadFilePattern);
                Path base = Paths.get(project.getBasePath());

                Path downloadRoot = base.resolve(path);
                while (downloadRoot != null) {
                    if (!downloadRoot.toString().contains("$")) {
                        break;
                    }

                    downloadRoot = downloadRoot.getParent();
                }

                Path filePath = Paths.get(file.getPath());

                if (downloadFolderOnly && !filePath.startsWith(downloadRoot)) {
                    // we're not in download folder
                    return false;
                }

                final VirtualFile parent = file.getParent();
                return parent != null && projectScope.contains(file);
            }

            @Override
            public boolean isSearchInLibraries() {
                return false;
            }
        };
    }

    public static List<VirtualFile> getVirtualFiles(String oid, Project project, boolean downloadFolderOnly) {
        if (oid == null) {
            return Collections.emptyList();
        }

        Collection<VirtualFile> files = FileBasedIndex.getInstance()
                .getContainingFiles(NAME, oid, createFilter(project, downloadFolderOnly));

        return new ArrayList<>(files);
    }

    public static List<OidNameValue> getOidNamesByOid(String oid, Project project) {
        return getOidNamesByOid(oid, project, false);
    }

    public static List<OidNameValue> getOidNamesByOid(String oid, Project project, boolean downloadFolderOnly) {
        if (oid == null) {
            return Collections.emptyList();
        }

        Collection<OidNameValue> collection = FileBasedIndex.getInstance()
                .getValues(NAME, oid, createFilter(project, downloadFolderOnly));

        return new ArrayList<>(collection);
    }

    public static List<OidNameValue> getAllOidNames(Project project) {
        Collection<String> keys = FileBasedIndex.getInstance().getAllKeys(NAME, project);

        List<OidNameValue> values = new ArrayList<>();
        keys.forEach(k -> values.addAll(getOidNamesByOid(k, project)));

        return values;
    }
}
