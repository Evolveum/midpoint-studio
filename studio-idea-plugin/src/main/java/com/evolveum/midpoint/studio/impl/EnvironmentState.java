package com.evolveum.midpoint.studio.impl;

import com.evolveum.midpoint.studio.client.ProxyType;
import com.evolveum.midpoint.studio.util.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Viliam Repan (lazyman).
 */
public class EnvironmentState implements Serializable {

    private String id;

    private String name;

    private Color color;

    private String shortName;

    private String url;

    private boolean ignoreSslErrors;

    private String proxyServerHost;

    private Integer proxyServerPort;

    private ProxyType proxyServerType = ProxyType.HTTP;

    private boolean useHttp2;

    private List<String> nodeUrls;

    private String propertiesFilePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAwtColor(java.awt.Color color) {
        if (color == null) {

        }
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isIgnoreSslErrors() {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors) {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public String getProxyServerHost() {
        return proxyServerHost;
    }

    public void setProxyServerHost(String proxyServerHost) {
        this.proxyServerHost = proxyServerHost;
    }

    public Integer getProxyServerPort() {
        return proxyServerPort;
    }

    public void setProxyServerPort(Integer proxyServerPort) {
        this.proxyServerPort = proxyServerPort;
    }

    public ProxyType getProxyServerType() {
        return proxyServerType;
    }

    public void setProxyServerType(ProxyType proxyServerType) {
        this.proxyServerType = proxyServerType;
    }

    public boolean isUseHttp2() {
        return useHttp2;
    }

    public void setUseHttp2(boolean useHttp2) {
        this.useHttp2 = useHttp2;
    }

    public List<String> getNodeUrls() {
        return nodeUrls;
    }

    public void setNodeUrls(List<String> nodeUrls) {
        this.nodeUrls = nodeUrls;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public void setPropertiesFilePath(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }

    public EnvironmentState copy() {
        EnvironmentState other = new EnvironmentState();
        other.id = id;
        other.name = name;
        other.color = color != null ? color.copy() : null;
        other.shortName = shortName;
        other.url = url;
        other.ignoreSslErrors = ignoreSslErrors;
        other.proxyServerHost = proxyServerHost;
        other.proxyServerPort = proxyServerPort;
        other.proxyServerType = proxyServerType;
        other.useHttp2 = useHttp2;
        other.nodeUrls = nodeUrls != null ? new ArrayList<>(nodeUrls) : null;
        other.propertiesFilePath = propertiesFilePath;

        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnvironmentState that = (EnvironmentState) o;

        if (ignoreSslErrors != that.ignoreSslErrors) return false;
        if (useHttp2 != that.useHttp2) return false;
        if (!Objects.equals(id, that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(color, that.color)) return false;
        if (!Objects.equals(shortName, that.shortName)) return false;
        if (!Objects.equals(url, that.url)) return false;
        if (!Objects.equals(proxyServerHost, that.proxyServerHost))
            return false;
        if (!Objects.equals(proxyServerPort, that.proxyServerPort))
            return false;
        if (proxyServerType != that.proxyServerType) return false;
        if (!Objects.equals(nodeUrls, that.nodeUrls)) return false;
        return Objects.equals(propertiesFilePath, that.propertiesFilePath);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (ignoreSslErrors ? 1 : 0);
        result = 31 * result + (proxyServerHost != null ? proxyServerHost.hashCode() : 0);
        result = 31 * result + (proxyServerPort != null ? proxyServerPort.hashCode() : 0);
        result = 31 * result + (proxyServerType != null ? proxyServerType.hashCode() : 0);
        result = 31 * result + (useHttp2 ? 1 : 0);
        result = 31 * result + (nodeUrls != null ? nodeUrls.hashCode() : 0);
        result = 31 * result + (propertiesFilePath != null ? propertiesFilePath.hashCode() : 0);
        return result;
    }
}
