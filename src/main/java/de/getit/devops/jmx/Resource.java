/*
 * Project: Deichmann
 * Copyright© 2016 getit GmbH
 *
 * Creation: 23.05.16 by dennis.ploeger
 */
package de.getit.devops.jmx;

public class Resource {

    private String host;
    private String path;

    public Resource(String host, String path) {
        this.host = host;
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
