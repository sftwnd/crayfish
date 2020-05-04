package com.github.sftwnd.crayfish.zookeeper;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ZookeeperHelper {

    private static final char PATH_SEPARATOR = '/';

    @Getter(AccessLevel.PROTECTED) private final CuratorFramework curatorFramework;
    @Getter(AccessLevel.PROTECTED) private final String path;

    public ZookeeperHelper(@Nonnull final ZookeeperService zookeeperService, @Nonnull String path) {
        this(Objects.requireNonNull(zookeeperService).getCuratorFramework(), path);
    }

    public ZookeeperHelper(@Nonnull final CuratorFramework curatorFramework, @Nonnull String path) {
        this.curatorFramework = Objects.requireNonNull(curatorFramework);
        this.path = Objects.requireNonNull(path);
    }

    public final String getAbsolutePath(String name) {
        return getAbsolutePath(getPath(), name);
    }

    public final void setData(@Nonnull String name, @Nullable byte[] buff) throws Exception {
        if (buff == null) {
            curatorFramework.setData().forPath(name);
        } else {
            curatorFramework.setData().forPath(name, buff);
        }
    }

    public static final String getAbsolutePath(String path, String name) {
        if (path == null || path.length() == 0) {
            return name;
        } else if (name == null || name.length() == 0) {
            return path;
        } else if (path.charAt(path.length()-1) == PATH_SEPARATOR) {
            return getAbsolutePath(path.substring(0, path.length()-1), name);
        }else if (name.startsWith(String.valueOf(PATH_SEPARATOR))) {
            return getAbsolutePath(path, name.substring(1));
        } else {
            return new StringBuilder(path).append(PATH_SEPARATOR).append(name).toString();
        }
    }
}
