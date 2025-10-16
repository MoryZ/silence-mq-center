
package com.old.silence.mq.center.domain.model;

import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;

public class ConnectionInfo extends Connection {
    private String versionDesc;

    public static ConnectionInfo buildConnectionInfo(Connection connection) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setClientId(connection.getClientId());
        connectionInfo.setClientAddr(connection.getClientAddr());
        connectionInfo.setLanguage(connection.getLanguage());
        connectionInfo.setVersion(connection.getVersion());
        connectionInfo.setVersionDesc(MQVersion.getVersionDesc(connection.getVersion()));
        return connectionInfo;
    }

    public static HashSet<Connection> buildConnectionInfoHashSet(Collection<Connection> connectionList) {
        HashSet<Connection> connectionHashSet = Sets.newHashSet();
        for (Connection connection : connectionList) {
            connectionHashSet.add(buildConnectionInfo(connection));
        }
        return connectionHashSet;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }
}
