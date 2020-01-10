package com.github.sftwnd.crayfish.zookeeper.info;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.info.BaseNamedInfo;
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
    @SuppressWarnings("unchecked")
    public void save(@Nonnull NamedInfo<I> data) throws Exception {
        byte[] buff = Optional.ofNullable(Objects.requireNonNull(data).getInfo())
                     .map(i -> String.class.equals(clazz) ? i.toString() : ExceptionUtils.wrapUncheckedExceptions(() -> JsonMapper.serializeObject(i)))
                     .map(String::getBytes)
                     .orElse(null);
        if (buff == null) {
            getCuratorFramework().setData().forPath(getAbsolutePath(data.getName()));
        } else {
            getCuratorFramework().setData().forPath(getAbsolutePath(data.getName()), buff);
        }
    }

    public NamedInfoSaver<I> getSaver(final String additionalPath) {
        final NamedInfoSaver<I> baseSaver = this;
        return data -> baseSaver.save(new BaseNamedInfo<>(getAbsolutePath(additionalPath, data.getName()), data.getInfo()));
    }

}
