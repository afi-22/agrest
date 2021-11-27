package io.agrest.meta;

import io.agrest.filter.CreateFilter;
import io.agrest.filter.DeleteFilter;
import io.agrest.filter.ReadFilter;
import io.agrest.filter.UpdateFilter;
import io.agrest.filter.PropertyFilteringRulesBuilder;
import io.agrest.filter.PropertyFilter;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.NestedDataResolverFactory;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.RootDataResolverFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * A mutable collection of entity properties that allow the application code to override and customize {@link AgEntity}
 * structure either globally or per-request.
 *
 * @since 1.12
 */
public class AgEntityOverlay<T> {

    private final Class<T> type;
    private final Map<String, AgAttributeOverlay> attributes;
    private final Map<String, AgRelationshipOverlay> relationships;
    private RootDataResolver<T> rootDataResolver;

    private PropertyFilter readablePropFilter;
    private PropertyFilter writablePropFilter;

    private ReadFilter<T> readFilter;
    private CreateFilter<T> createFilter;
    private UpdateFilter<T> updateFilter;
    private DeleteFilter<T> deleteFilter;

    public AgEntityOverlay(Class<T> type) {
        this.type = type;
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
        this.readFilter = ReadFilter.allowsAllFilter();
        this.createFilter = CreateFilter.allowsAllFilter();
        this.updateFilter = UpdateFilter.allowsAllFilter();
        this.deleteFilter = DeleteFilter.allowsAllFilter();
    }

    private static PropertyReader fromFunction(Function<?, ?> f) {
        // lose generics. PropertyReader is not parameterized
        Function fx = f;
        return fx::apply;
    }

    private static <T> NestedDataResolver<T> resolverForReader(Function<?, T> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    static <T> NestedDataResolver<T> resolverForListReader(Function<?, List<T>> reader) {
        return new ReaderBasedResolver<>(fromFunction(reader));
    }

    /**
     * Resolves entity overlay to an entity.
     *
     * @since 4.8
     */
    public AgEntity<T> resolve(AgDataMap agDataMap, AgEntity<T> maybeOverlaid) {

        // TODO: support null entity like we do for overlaid Attributes and Relationships?
        Objects.requireNonNull(maybeOverlaid);

        if (isEmpty()) {
            return maybeOverlaid;
        }

        AgEntityOverlayResolver resolver = new AgEntityOverlayResolver(agDataMap, maybeOverlaid);

        getAttributeOverlays().forEach(resolver::loadAttributeOverlay);
        getRelationshipOverlays().forEach(resolver::loadRelationshipOverlay);

        if (readablePropFilter != null) {
            PropertyFilteringRulesBuilder pa = new PropertyFilteringRulesBuilder();
            readablePropFilter.apply(pa);
            pa.resolveInaccessible(maybeOverlaid, this).forEach(resolver::makeUnreadable);
        }

        if (writablePropFilter != null) {
            PropertyFilteringRulesBuilder pa = new PropertyFilteringRulesBuilder();
            writablePropFilter.apply(pa);
            pa.resolveInaccessible(maybeOverlaid, this).forEach(resolver::makeUnwritable);
        }

        return new DefaultAgEntity<>(
                maybeOverlaid.getName(),
                type,
                resolver.ids,
                resolver.attributes,
                resolver.relationships,
                rootDataResolver != null ? rootDataResolver : maybeOverlaid.getDataResolver(),
                maybeOverlaid.getReadFilter().andThen(readFilter),
                maybeOverlaid.getCreateFilter().andThen(createFilter),
                maybeOverlaid.getUpdateFilter().andThen(updateFilter),
                maybeOverlaid.getDeleteFilter().andThen(deleteFilter)
        );
    }

    private boolean isEmpty() {
        return attributes.isEmpty()
                && relationships.isEmpty()
                && readablePropFilter == null
                && writablePropFilter == null
                && readFilter.allowsAll()
                && createFilter.allowsAll()
                && updateFilter.allowsAll()
                && deleteFilter.allowsAll();
    }

    /**
     * Combines this overlay with another overlay. This overlay is modified, loading overlaid properties and
     * resolvers from another overlay.
     *
     * @return this overlay
     * @since 2.10
     */
    public AgEntityOverlay<T> merge(AgEntityOverlay<T> anotherOverlay) {
        attributes.putAll(anotherOverlay.attributes);
        relationships.putAll(anotherOverlay.relationships);

        if (anotherOverlay.readablePropFilter != null) {
            readablePropFilter(anotherOverlay.readablePropFilter);
        }

        if (anotherOverlay.writablePropFilter != null) {
            writablePropFilter(anotherOverlay.writablePropFilter);
        }

        if (anotherOverlay.getRootDataResolver() != null) {
            this.rootDataResolver = anotherOverlay.getRootDataResolver();
        }

        this.readFilter = this.readFilter.andThen(anotherOverlay.readFilter);
        this.createFilter = this.createFilter.andThen(anotherOverlay.createFilter);
        this.updateFilter = this.updateFilter.andThen(anotherOverlay.updateFilter);
        this.deleteFilter = this.deleteFilter.andThen(anotherOverlay.deleteFilter);

        return this;
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 4.7
     */
    public AgAttributeOverlay getAttributeOverlay(String name) {
        return attributes.get(name);
    }

    /**
     * @since 4.7
     */
    public Iterable<AgAttributeOverlay> getAttributeOverlays() {
        return attributes.values();
    }

    /**
     * @since 3.4
     */
    public AgRelationshipOverlay getRelationshipOverlay(String name) {
        return relationships.get(name);
    }

    /**
     * @since 3.4
     */
    public Iterable<AgRelationshipOverlay> getRelationshipOverlays() {
        return relationships.values();
    }

    /**
     * @since 3.4
     */
    public RootDataResolver<T> getRootDataResolver() {
        return rootDataResolver;
    }

    /**
     * Adds a {@link PropertyFilter} to any existing read filters in this overlay.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> readablePropFilter(PropertyFilter filter) {
        this.readablePropFilter = readablePropFilter != null ? readablePropFilter.andThen(filter) : filter;
        return this;
    }

    /**
     * Adds a {@link PropertyFilter} to any existing write filters in this overlay.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> writablePropFilter(PropertyFilter filter) {
        this.writablePropFilter = writablePropFilter != null ? writablePropFilter.andThen(filter) : filter;
        return this;
    }

    /**
     * Adds an object READ filter to the existing filters.
     *
     * @since 4.8
     */
    public AgEntityOverlay<T> readFilter(ReadFilter<T> filter) {
        this.readFilter = this.readFilter.andThen(filter);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> createFilter(CreateFilter<T> filter) {
        this.createFilter = this.createFilter.andThen(filter);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> updateFilter(UpdateFilter<T> filter) {
        this.updateFilter = this.updateFilter.andThen(filter);
        return this;
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> deleteFilter(DeleteFilter<T> filter) {
        this.deleteFilter = this.deleteFilter.andThen(filter);
        return this;
    }

    /**
     * Removes a named property (attribute or relationship) from overlaid AgEntity.
     *
     * @return this overlay instance
     * @since 3.7
     * @deprecated since 4.8 use {@link #readablePropFilter(PropertyFilter)} and/or {@link #writablePropFilter(PropertyFilter)}
     */
    @Deprecated
    public AgEntityOverlay<T> exclude(String... properties) {
        List<String> asList = asList(properties);
        readablePropFilter(pa -> asList.forEach(p -> pa.property(p, false)));
        writablePropFilter(pa -> asList.forEach(p -> pa.property(p, false)));
        return this;
    }

    /**
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttributeOverlay(name, type, valueType, null, null, fromFunction(reader)));
        return this;
    }

    /**
     * Adds or replaces an attribute in the overlaid entity.
     *
     * @since 4.7
     */
    public <V> AgEntityOverlay<T> redefineAttribute(String name, Class<V> valueType, boolean readable, boolean writable, Function<T, V> reader) {
        attributes.put(name, new DefaultAgAttributeOverlay(name, type, valueType, readable, writable, fromFunction(reader)));
        return this;
    }

    /**
     * Redefines nested resolver for a named relationship.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, null, null, null, null, resolver));
        return this;
    }

    /**
     * Adds an overlay for an existing named relationship, replacing its default resolving strategy.
     *
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRelationshipResolver(String name, Function<T, ?> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, null, null, null, null, resolverForReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, null, null, resolver));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 4.7
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, boolean readable, boolean writable, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, readable, writable, resolver));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, null, null, resolver));
        return this;
    }

    /**
     * Adds or replaces a relationship overlay in the overlaid entity. This allows Agrest entities to declare properties not
     * present in the underlying Java objects or change how the standard relationships are read.
     *
     * @since 4.7
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, boolean readable, boolean writable, NestedDataResolverFactory resolverFactory) {
        NestedDataResolver<?> resolver = resolverFactory.resolver(type, name);
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, readable, writable, resolver));
        return this;
    }

    /**
     * Adds or replaces a to-one relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, Function<T, V> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, null, null, resolverForReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a to-one relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 4.7
     */
    public <V> AgEntityOverlay<T> redefineToOne(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, V> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, false, readable, writable, resolverForReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a to-many relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 3.4
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, Function<T, List<V>> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, null, null, resolverForListReader(reader)));
        return this;
    }

    /**
     * Adds or replaces a to-many relationship in the overlaid entity. The value of the relationship will be
     * calculated from each entity object by applying the "reader" function. This allows Agrest entities
     * to declare properties not present in the underlying Java objects or change how the standard relationships are
     * read.
     *
     * @since 4.7
     */
    public <V> AgEntityOverlay<T> redefineToMany(String name, Class<V> targetType, boolean readable, boolean writable, Function<T, List<V>> reader) {
        relationships.put(name, new DefaultAgRelationshipOverlay(name, type, targetType, true, readable, writable, resolverForListReader(reader)));
        return this;
    }

    /**
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRootDataResolver(RootDataResolverFactory rootDataResolverFactory) {
        this.rootDataResolver = rootDataResolverFactory.resolver(type);
        return this;
    }

    /**
     * @since 3.4
     */
    public AgEntityOverlay<T> redefineRootDataResolver(RootDataResolver<T> rootDataResolver) {
        this.rootDataResolver = rootDataResolver;
        return this;
    }
}
