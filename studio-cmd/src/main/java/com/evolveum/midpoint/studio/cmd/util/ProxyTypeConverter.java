package com.evolveum.midpoint.studio.cmd.util;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import com.evolveum.midpoint.studio.client.ProxyType;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ProxyTypeConverter extends BaseConverter<ProxyType> {

    public ProxyTypeConverter(String optionName) {
        super(optionName);
    }

    @Override
    public ProxyType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return ProxyType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ParameterException(getErrorString(value, "a proxy type (http/socks)"));
        }
    }
}
