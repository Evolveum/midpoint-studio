package com.evolveum.midpoint.studio.cmd.util;

import com.beust.jcommander.JCommander;
import com.evolveum.midpoint.studio.cmd.Command;
import com.evolveum.midpoint.studio.cmd.opts.BaseOptions;
import com.evolveum.midpoint.studio.cmd.opts.EnvironmentOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class StudioUtil {

    public static final String XML_OBJECTS_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<c:objects xmlns=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\"\n" +
            "\txmlns:c=\"http://midpoint.evolveum.com/xml/ns/public/common/common-3\"\n" +
            "\txmlns:org=\"http://midpoint.evolveum.com/xml/ns/public/common/org-3\">\n";

    public static final String XML_OBJECTS_SUFFIX = "</c:objects>";

    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".##");

    public static JCommander setupCommandLineParser() {
        BaseOptions base = new BaseOptions();
        EnvironmentOptions environment = new EnvironmentOptions();

        JCommander.Builder builder = JCommander.newBuilder()
                .expandAtSign(false)
                .addObject(base)
                .addObject(environment);

        for (Command cmd : Command.values()) {
            builder.addCommand(cmd.getCommandName(), cmd.createOptions());
        }

        JCommander jc = builder.build();
        jc.setProgramName("java -jar studio-cmd.jar");
        jc.setColumnSize(150);
        jc.setAtFileCharset(Charset.forName(base.getCharset()));

        return jc;
    }

    public static <T> T getOptions(JCommander jc, Class<T> type) {
        List<Object> objects = jc.getObjects();
        for (Object object : objects) {
            if (type.equals(object.getClass())) {
                return (T) object;
            }
        }

        return null;
    }

//    public static ObjectFilter createObjectFilter(FileReference strFilter, StudioContext context, Class<? extends ObjectType> objectClass)
//            throws IOException, SchemaException {
//        ObjectQuery query = createObjectQuery(strFilter, context, objectClass);
//        return query != null ? query.getFilter() : null;
//    }
//
//    public static ObjectQuery createObjectQuery(FileReference ref, StudioContext context, Class<? extends ObjectType> objectClass)
//            throws IOException, SchemaException {
//
//        if (ref == null) {
//            return null;
//        }
//
//        String filterStr = ref.getValue();
//        if (ref.getReference() != null) {
//            File file = ref.getReference();
//            filterStr = FileUtils.readFileToString(file, context.getCharset());
//        }
//
//        PrismContext prismContext = context.getPrismContext();
//        PrismParserNoIO parser = prismContext.parserFor(filterStr);
//        RootXNode root = parser.parseToXNode();
//
//        ObjectFilter filter = context.getQueryConverter().parseFilter(root.toMapXNode(), objectClass);
//        return prismContext.queryFactory().createQuery(filter);
//    }

    public static String printStackToString(Exception ex) {
        if (ex == null) {
            return null;
        }

        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }

    public static File[] listFiles(FileReference ref, String... extensions) {
        if (ref.getReference() == null) {
            return new File[0];
        }

        List<File> result = new ArrayList<>();

        File file = ref.getReference();

        if (!file.exists()) {
            return new File[0];
        }

        if (file.isDirectory()) {
            result.addAll(FileUtils.listFiles(file, extensions, true));
        } else {
            String ext = FilenameUtils.getExtension(file.getName());
            if (Arrays.asList(extensions).contains(ext)) {
                result.add(file);
            }
        }

        return result.toArray(new File[result.size()]);
    }
}
