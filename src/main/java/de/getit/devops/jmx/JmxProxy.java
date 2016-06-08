/*
 * Project: Deichmann
 * Copyright© 2016 getit GmbH
 *
 * Creation: 23.05.16 by dennis.ploeger
 */
package de.getit.devops.jmx;

import com.j256.simplejmx.client.JmxClient;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class JmxProxy {

    private final String jmxUrl;
    private final JmxClient client;

    public JmxProxy(final String jmxUrl) throws JMException {
        this.jmxUrl = jmxUrl;
        this.client = new JmxClient(this.jmxUrl);
    }

    public JmxProxy(final String jmxUrl, final String username, final String password) throws JMException {

        this.jmxUrl = jmxUrl;
        this.client = new JmxClient(
            this.jmxUrl,
            username,
            password
        );

    }

    public boolean isResourceStarted(final Resource resource) throws Exception {

        final Hashtable<String, String> beanKeys = new Hashtable<>();

        beanKeys.put("name",
            String.format("//%s%s",
                resource.getHost(),
                resource.getPath()
            )
        );

        beanKeys.put(
            "J2EEApplication",
            "none"
        );

        beanKeys.put(
            "J2EEServer",
            "none"
        );

        beanKeys.put(
            "j2eeType",
            "WebModule"
        );

        final ObjectName bean = new ObjectName("Catalina", beanKeys);

        try {

            final String currentState = (String) this.client.getAttribute(
                bean,
                "stateName"
            );

            return "STARTED".equalsIgnoreCase(currentState);

        } catch (final InstanceNotFoundException e) {
            // Server is starting. Ignore

            return false;
        }

    }

}
