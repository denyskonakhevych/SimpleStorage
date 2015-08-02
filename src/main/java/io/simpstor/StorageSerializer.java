package io.simpstor;

import com.google.common.base.Optional;

/**
 * Created by koxa on 02.08.2015.
 */
public interface StorageSerializer {

    <T> Optional<T> getSerializer();
}
