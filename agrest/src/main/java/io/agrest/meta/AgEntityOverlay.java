package io.agrest.meta;

import io.agrest.annotation.AgId;
import io.agrest.meta.compiler.BeanAnalyzer;
import io.agrest.meta.compiler.PropertyGetter;
import io.agrest.property.PropertyReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A mutable collection of entity properties that are not derived from the object structure. {@link AgEntityOverlay}
 * objects are provided to Agrest by the app, and are merged into corresponding {@link AgEntity} entities.
 *
 * @since 1.12
 */
public class AgEntityOverlay<T> {

    private Class<T> type;
    private Map<String, AgAttribute> attributes;
    private Map<String, Function<AgDataMap, AgRelationship>> relationships;
    private Map<String, PropertyGetter> typeGetters;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    // lose generics ... PropertyReader is not parameterized
    private static PropertyReader asPropertyReader(Function reader) {
        return (o, n) -> reader.apply(o);
    }

    /**
     * @since 2.10
     */
    public AgEntityOverlay<T> merge(AgEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);
        return this;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 2.10
     */
    public Iterable<AgAttribute> getAttributes() {
        return attributes.values();
    }

    /**
     * @since 2.10
     */
    public Stream<AgRelationship> getRelationships(AgDataMap dataMap) {
        // resolve relationship targets
        return relationships.values().stream().map(f -> f.apply(dataMap));
    }

    private Map<String, PropertyGetter> getTypeGetters() {

        // compile getters map lazily, only when the caller adds attributes that require getters
        if (this.typeGetters == null) {
            Map<String, PropertyGetter> getters = new HashMap<>();

            // this is expensive... still not caching, as presumably overlays are processed only once
            BeanAnalyzer.findGetters(type).forEach(pm -> getters.put(pm.getName(), pm));

            this.typeGetters = getters;
        }

        return typeGetters;
    }

    /**
     * Adds an attribute to the overlaid entity. The value of the attribute will be read from the object itself.
     * This overlay is only needed if Agrest can't otherwise determine property presence in the entity.
     * An alternative to calling this method explicitly is annotating property getters with
     * {@link io.agrest.annotation.AgAttribute}, {@link io.agrest.annotation.AgRelationship} or
     * {@link AgId}. Also all Cayenne attributes are automatically added to the entity.
     *
     * @since 2.10
     */
    public AgEntityOverlay<T> addAttribute(String name) {

        PropertyGetter getter = getTypeGetters().get(name);

        if (getter == null) {
            throw new IllegalArgumentException("'" + name + "' is not a readable property in " + type.getName());
        }

        Class vType = getter.getType();
        addAttribute(name, vType, getter::getValue);
        return this;
    }

    /**
     * Adds an "ad-hoc" attribute to the overlaid entity. The value of the attribute will be calculated from
     * each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> AgEntityOverlay<T> addAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttribute(name, valueType, asPropertyReader(reader)));
        return this;
    }

    /**
     * Adds an "ad-hoc" to-one relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> AgEntityOverlay<T> addToOneRelationship(String name, Class<V> valueType, Function<T, V> reader) {
        relationships.put(name, dm -> resolveToOne(dm, name, valueType, reader));
        return this;
    }

    /**
     * Adds an "ad-hoc" to-many relationship to the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects.
     *
     * @since 2.10
     */
    public <V> AgEntityOverlay<T> addToManyRelationship(String name, Class<V> valueType, Function<T, List<V>> reader) {
        relationships.put(name, dm -> resolveToMany(dm, name, valueType, reader));
        return this;
    }

    private <V> AgRelationship resolveToOne(AgDataMap dataMap, String name, Class<V> type, Function<T, V> reader) {
        AgEntity<V> target = dataMap.getEntity(type);
        return new DefaultAgRelationship(name, target, false, e -> asPropertyReader(reader));
    }

    private <V> AgRelationship resolveToMany(AgDataMap dataMap, String name, Class<V> type, Function<T, List<V>> reader) {
        AgEntity<V> target = dataMap.getEntity(type);
        return new DefaultAgRelationship(name, target, true, e -> asPropertyReader(reader));
    }
}
