package com.github.sftwnd.crayfish.zookeeper;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import com.github.sftwnd.crayfish.common.i18n.I18n;
import com.github.sftwnd.crayfish.common.i18n.MessageSource;
import com.github.sftwnd.crayfish.common.info.BaseNamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfo;
import com.github.sftwnd.crayfish.common.info.NamedInfoLoader;
import com.github.sftwnd.crayfish.common.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class ZookeeperNamedInfoLoader<I extends Cloneable> implements NamedInfoLoader<I> {

    private static final MessageSource messageSource = I18n.getMessageSource();
    private static final String throwsExceptionWhenParseJson = "crayfish-zookeeper.throwsExceptionWhenParseJson";
    private static final String throwsExceptionWhenParseJsonMsg = "Невозможно преобразовать к объекту {} строковое JSON представление: «{}». Реакция на ошибку: «{}»";
    private static final String unableToCleanNode="crayfish-zookeeper.unableToCleanNode";
    private static final String unableToCleanNodeMsg="Невозможно очистить данные узла: «{}» по пути: «{}». Реакция на ошибку: «{}»";

    private final CuratorFramework curatorFramework;
    private final Class<I> clazz;
    private final String path;

    public ZookeeperNamedInfoLoader(@Nonnull final ZookeeperService zookeeperService, @Nonnull  Class<I> clazz, @Nonnull String path) {
        Objects.requireNonNull(zookeeperService);
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(path);
        this.curatorFramework = zookeeperService.getCuratorFramework();
        this.clazz = clazz;
        this.path = path;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<NamedInfo<I>> apply(OnThrow onThrow) {
        try {
            return curatorFramework.getChildren().forPath(path).stream()
                      .map( nodeName -> {
                               String nodePath = new StringBuilder(path).append('/').append(nodeName).toString();
                               byte[] nodeData;
                               NamedInfo<I> result = null;
                               try {
                                   nodeData = curatorFramework.getData().forPath(nodePath);
                               } catch (KeeperException.NoNodeException nndex) {
                                   return result;
                               } catch (Exception ex) {
                                   return ExceptionUtils.uncheckExceptions(ex);
                               }
                               if (nodeData == null) {
                                   result = new BaseNamedInfo<>(nodeName, (I)null);
                               } else if (String.class.equals(clazz)) {
                                   result = new BaseNamedInfo<>(nodeName, (I)((Object)new String(nodeData, StandardCharsets.UTF_8)));
                               } else {
                                   try {
                                       result =  new BaseNamedInfo<>(nodeName, JsonMapper.parseObject(nodeData, clazz));
                                   } catch (IOException ioex) {
                                       logger.warn(messageSource.messageDef(throwsExceptionWhenParseJson, throwsExceptionWhenParseJsonMsg), clazz, new String(nodeData, StandardCharsets.UTF_8), ioex.getLocalizedMessage());
                                       switch (onThrow) {
                                           case THROW:
                                           default:
                                               return ExceptionUtils.uncheckExceptions(ioex);
                                           case CLEAN:
                                               try {
                                                   clearProperty(nodePath);
                                               } catch (Exception expt) {
                                                   logger.warn(messageSource.messageDef(unableToCleanNode, unableToCleanNodeMsg), nodeName, path);
                                               }
                                           case EMPTY:
                                               result =  new BaseNamedInfo<>(nodeName,  null);
                                           case SKIP:
                                               break;
                                       }
                                   }
                               }
                               return result;
                           }
                      ).filter(n -> n != null);
        } catch (Exception ex) {
            return ExceptionUtils.uncheckExceptions(ex);
        }
    }

    private void clearProperty(String nodePath) throws Exception {
        curatorFramework.setData().forPath(nodePath);
    }

}
