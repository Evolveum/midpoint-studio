package com.evolveum.midpoint.studio.impl.ide;

import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.studio.impl.MidPointFacet;
import com.evolveum.midpoint.studio.impl.MidPointFacetConfiguration;
import com.evolveum.midpoint.studio.impl.MidPointFacetType;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointFacetFrameworkDetector extends FacetBasedFrameworkDetector<MidPointFacet, MidPointFacetConfiguration> {

    public MidPointFacetFrameworkDetector() {
        super(MidPointFacetType.FACET_ID);
    }

    @Override
    public FacetType<MidPointFacet, MidPointFacetConfiguration> getFacetType() {
        return FacetTypeRegistry.getInstance().findFacetType(MidPointFacetType.FACET_ID);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return XmlFileType.INSTANCE;
    }

    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern() {
        return FileContentPattern.fileContent().xmlWithRootTagNamespace(SchemaConstants.NS_C);
    }
}
