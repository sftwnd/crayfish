package com.github.sftwnd.crayfish.zookeeper.info;

import com.github.sftwnd.crayfish.common.info.NamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfoSaver;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import com.github.sftwnd.crayfish.zookeeper.ZookeeperHelper;
import com.github.sftwnd.crayfish.zookeeper.ZookeeperService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;

@Slf4j
public class ZookeeperNamedInfoSaver<I> extends ZookeeperHelper implements NamedInfoSaver<I> {

  //private static final MessageSource messageSource = I18n.getMessageSource();
    private final Class<I> clazz;

    public ZookeeperNamedInfoSaver(@Nonnull final ZookeeperService zookeeperService, @Nonnull Class<I> clazz, @Nonnull String path) {
        this(Objects.requireNonNull(zookeeperService).getCuratorFramework(), clazz, path);
    }

    public ZookeeperNamedInfoSaver(@Nonnull final CuratorFramework curatorFramework, @Nonnull Class<I> clazz, @Nonnull String path) {
        super(curatorFramework, path);
        this.clazz = Objects.requireNonNull(clazz);
    }

    @Override
    public void save(@Nonnull NamedInfo<I> data) throws Exception {
        Objects.requireNonNull(data, "ZookeeperNamedInfoSaver::save - data is null");
        setData( Objects.requireNonNull(data.getName(), "ZookeeperNamedInfoSaver::save - data.name is null")
                ,Optional.of(Objects.requireNonNull(data).getInfo())
                        .map(i -> String.class.equals(clazz) ? i.toString() : wrapUncheckedExceptions(() -> JsonMapper.serializeObject(i)))
                        .map(String::getBytes)
                        .orElse(null)
                );
    }

    public <T>NamedInfoSaver<T> constructSaver(final String additionalPath, Class<T> clazz) {
        return new ZookeeperNamedInfoSaver<>(this.getCuratorFramework(), clazz, getAbsolutePath(additionalPath))::save;
    }

}
