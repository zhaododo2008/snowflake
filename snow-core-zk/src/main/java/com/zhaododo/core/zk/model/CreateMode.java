package com.zhaododo.core.zk.model;

public enum CreateMode {
    PERSISTENT(0), PERSISTENT_SEQUENTIAL(1), EPHEMERAL(2), EPHEMERAL_SEQUENTIAL(3);

    private final int value;

    private CreateMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static org.apache.zookeeper.CreateMode findByValue(int value) {
        switch (value) {
            case 0:
                return org.apache.zookeeper.CreateMode.PERSISTENT;
            case 1:
                return org.apache.zookeeper.CreateMode.PERSISTENT_SEQUENTIAL;
            case 2:
                return org.apache.zookeeper.CreateMode.EPHEMERAL;
            case 3:
                return org.apache.zookeeper.CreateMode.EPHEMERAL_SEQUENTIAL;
            default:
                return null;
        }
    }
}
