package com.github.sftwnd.crayfish.zookeeper.info;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.info.BaseNamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfoLoadEception;
import com.github.sftwnd.crayfish.common.info.NamedInfoLoader;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import com.github.sftwnd.crayfish.zookeeper.ZookeeperHelper;
import com.github.sftwnd.crayfish.zookeeper.ZookeeperService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class ZookeeperNamedInfoLoader<I> extends ZookeeperHelper implements NamedInfoLoader<I> {

    @Getter private final Class<I> clazz;
    @Getter private final NamedInfoOnThrowListener<I, byte[], Throwable> onThrow;

    public ZookeeperNamedInfoLoader(@Nonnull final ZookeeperService zookeeperService, @Nonnull  Class<I> clazz, @Nonnull String path, @Nullable NamedInfoOnThrowListener<I, byte[], Throwable> onThrow) {
        this(Objects.requireNonNull(zookeeperService).getCuratorFramework(), clazz, path, onThrow);
    }

    public ZookeeperNamedInfoLoader(@Nonnull final CuratorFramework curatorFramework, @Nonnull  Class<I> clazz, @Nonnull String path, @Nullable NamedInfoOnThrowListener<I, byte[], Throwable> onThrow) {
        super(curatorFramework, path);
        this.clazz = Objects.requireNonNull(clazz);
        this.onThrow = onThrow;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<NamedInfo<I>> load() throws NamedInfoLoadEception {
        try {
            return getCuratorFramework().getChildren().forPath(getPath()).stream()
                      .map( nodeName -> {
                                  NamedInfo<I> result = null;
                                  byte[] nodeData = null;
                                  try {
                                      nodeData = getCuratorFramework().getData().forPath(getAbsolutePath(nodeName));
                                      if (nodeData == null || nodeData.length == 0) {
                                          result = new BaseNamedInfo<>(nodeName, null);
                                      } else if (String.class.equals(clazz)) {
                                          result = new BaseNamedInfo<>(nodeName, (I) (new String(nodeData, StandardCharsets.UTF_8)));
                                      } else {
                                          result = new BaseNamedInfo<>(nodeName, JsonMapper.parseObject(nodeData, clazz));
                                      }
                                  } catch (Exception expt) {
                                      result = onThrow == null ? (NamedInfo<I>)ExceptionUtils.uncheckExceptions(expt): onThrow.get(new BaseNamedInfo<>(nodeName, nodeData), expt);
                                  }
                                  return result;
                              }
                      ).filter(Objects::nonNull);
        } catch (Exception ex) {
            throw new NamedInfoLoadEception("Unable to load NamedInfo", ex);
        }
    }

    public NamedInfoLoader<I> getSaver(final String additionalPath) {
        return new ZookeeperNamedInfoLoader<>(getCuratorFramework(), clazz, getAbsolutePath(additionalPath), onThrow);
    }

}
