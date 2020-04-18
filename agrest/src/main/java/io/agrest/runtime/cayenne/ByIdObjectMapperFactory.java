package io.agrest.runtime.cayenne;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * A default singleton implementation of the {@link ObjectMapperFactory} that
 * looks up objects based on their IDs.
 *
 * @since 1.4
 */
public class ByIdObjectMapperFactory implements ObjectMapperFactory {

    private static final ObjectMapperFactory instance = new ByIdObjectMapperFactory();

    public static ObjectMapperFactory mapper() {
        return instance;
    }

    @Override
    public <T> ObjectMapper<T> createMapper(UpdateContext<T> context) {
        return new ByIdObjectMapper<>(context.getEntity().getAgEntity());
    }
}
