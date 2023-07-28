package io.agrest.meta;

import io.agrest.resolver.RelatedDataResolver;

public class DefaultRelationshipBuilder {
    private final String name;
    private final Class<?> sourceType;
    private final Class<?> targetType;
    private final Boolean toMany;
    private final Boolean readable;
    private final Boolean writable;
    private final RelatedDataResolver<?> resolver;

    public DefaultRelationshipBuilder(String name, Class<?> sourceType, Class<?> targetType, Boolean toMany, Boolean readable, Boolean writable, RelatedDataResolver<?> resolver) {
        this.name = name;
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.toMany = toMany;
        this.readable = readable;
        this.writable = writable;
        this.resolver = resolver;
    }

    public String getName() {
        return name;
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public Boolean getToMany() {
        return toMany;
    }

    public Boolean getReadable() {
        return readable;
    }

    public Boolean getWritable() {
        return writable;
    }

    public RelatedDataResolver<?> getResolver() {
        return resolver;
    }
}
