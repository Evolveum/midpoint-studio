package com.evolveum.midpoint.studio.client;

import com.evolveum.midpoint.schema.constants.ObjectTypes;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
public class MidPointObject {

    private String content;                        // XML content

    private ObjectTypes type;

    private boolean executable;

    private boolean delta;

    private File file;                    // used to derive output file names (and to delete files)

    private int objectIndex;                    // object number in the resource (if applicable)

    private String displayName;                    // how to identify object in messages, logs, etc.

    private boolean root;                        // is this a root element in the file?

    private boolean last;                        // is this a last one in the file?

    private boolean wholeFile;                // covers the whole file?

    private String oid;

    private String name;

    public MidPointObject(String content, ObjectTypes type, boolean executable) {
        this.content = content;
        this.type = type;
        this.executable = executable;
    }

    public static MidPointObject copy(MidPointObject object) {
        if (object == null) {
            return null;
        }

        MidPointObject o = new MidPointObject(object.getContent(), object.getType(), object.isExecutable());
        o.file = object.file;
        o.objectIndex = object.objectIndex;
        o.displayName = object.displayName;
        o.root = object.root;
        o.last = object.last;
        o.wholeFile = object.wholeFile;
        o.oid = object.oid;
        o.name = object.name;
        o.delta = object.delta;

        return o;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ObjectTypes getType() {
        return type;
    }

    public void setType(ObjectTypes type) {
        this.type = type;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getObjectIndex() {
        return objectIndex;
    }

    public void setObjectIndex(int objectIndex) {
        this.objectIndex = objectIndex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isWholeFile() {
        return wholeFile;
    }

    public void setWholeFile(boolean wholeFile) {
        this.wholeFile = wholeFile;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDelta() {
        return delta;
    }

    public void setDelta(boolean delta) {
        this.delta = delta;
    }
}
