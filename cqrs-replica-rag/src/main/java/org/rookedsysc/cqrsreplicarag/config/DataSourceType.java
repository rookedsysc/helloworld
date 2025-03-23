package org.rookedsysc.cqrsreplicarag.config;

public enum DataSourceType {
    WRITE,
    READ;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
