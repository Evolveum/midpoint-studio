package com.evolveum.midscribe.cmd;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.net.URI;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FileConverter implements IStringConverter<File>, IParameterValidator {

    private String optionName;

    public FileConverter() {
        this(null);
    }

    public FileConverter(String optionName) {
        this.optionName = optionName;
    }

    @Override
    public File convert(String value) {
        if (value == null) {
            return null;
        }

        try {
            return new File(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Option " + optionName
                    + " doesn't contain valid URL ('" + value + "')", ex);
        }
    }

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value == null) {
            return;
        }

        try {
            URI.create(value);
        } catch (IllegalArgumentException ex) {
            throw new ParameterException("Option " + name
                    + " doesn't contain valid URL ('" + value + "'), reason: " + ex.getMessage());
        }
    }
}
