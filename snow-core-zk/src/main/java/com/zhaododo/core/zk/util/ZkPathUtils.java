package com.zhaododo.core.zk.util;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

public final class ZkPathUtils {

    public static String getParentPath(String path) {
        Preconditions.checkNotNull(path, "path can't be null");
        if (!StringUtils.startsWith(path, "/")||StringUtils.endsWith(path, "/")) {
            throw new IllegalArgumentException("path is Illegal");
        }
        return path.substring(0,path.lastIndexOf("/"));
    }
}
