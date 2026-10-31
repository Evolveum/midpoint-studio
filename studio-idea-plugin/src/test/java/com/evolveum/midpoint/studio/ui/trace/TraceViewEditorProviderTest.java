package com.evolveum.midpoint.studio.ui.trace;

import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The trace view editor decides by itself which files it opens: the project must have the
 * MidPoint facet, the file must be an mptrace file or a zip archive whose first entry is a
 * trace. The decision does not depend on the registered file type.
 *
 * Each test gets its own light project (fresh descriptor instance) because some tests add
 * the facet to the module.
 */
public class TraceViewEditorProviderTest extends BasePlatformTestCase {

    private static final String TRACE_XML = """
            <tracingOutput xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <result>
                    <operation>com.example.TestOperation.run</operation>
                    <status>success</status>
                </result>
            </tracingOutput>
            """;

    private static final String OBJECT_XML = """
            <user xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3">
                <name>administrator</name>
            </user>
            """;

    private final LightProjectDescriptor descriptor = new LightProjectDescriptor();

    private final TraceViewEditorProvider provider = new TraceViewEditorProvider();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return descriptor;
    }

    public void testTraceFileWithoutFacetIsRejected() {
        VirtualFile file = addTextFile("traces/test.mptrace", TRACE_XML);

        assertFalse(provider.accept(getProject(), file));
    }

    public void testTraceFileWithFacetIsAccepted() {
        addMidPointFacet();
        VirtualFile file = addTextFile("traces/test.mptrace", TRACE_XML);

        assertTrue(provider.accept(getProject(), file));
    }

    public void testZipWithTraceEntryIsAccepted() throws IOException {
        addMidPointFacet();
        VirtualFile file = addBinaryFile("traces/trace.zip", zip("trace.xml", TRACE_XML));

        assertTrue(provider.accept(getProject(), file));
    }

    public void testZipWithOtherEntryIsRejected() throws IOException {
        addMidPointFacet();
        VirtualFile file = addBinaryFile("traces/objects.zip", zip("user.xml", OBJECT_XML));

        assertFalse(provider.accept(getProject(), file));
    }

    public void testXmlFileIsRejected() {
        addMidPointFacet();
        VirtualFile object = addTextFile("objects/user.xml", OBJECT_XML);
        VirtualFile trace = addTextFile("traces/trace.xml", TRACE_XML);

        assertFalse(provider.accept(getProject(), object));
        assertFalse(provider.accept(getProject(), trace));
    }

    private VirtualFile addTextFile(String path, String text) {
        return myFixture.addFileToProject(path, text).getVirtualFile();
    }

    private VirtualFile addBinaryFile(String path, byte[] content) throws IOException {
        VirtualFile file = myFixture.getTempDirFixture().createFile(path);
        WriteAction.run(() -> file.setBinaryContent(content));
        return file;
    }

    private static byte[] zip(String entryName, String entryContent) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(entryContent.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }

    private void addMidPointFacet() {
        FacetType<?, ?> facetType = FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_TYPE_ID);
        WriteAction.run(() -> FacetUtil.addFacet(getModule(), facetType, MidPointFacetType.FACET_NAME));
    }
}
