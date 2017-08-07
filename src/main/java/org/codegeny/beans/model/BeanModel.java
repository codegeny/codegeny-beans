package org.codegeny.beans.model;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An implementation of {@linkplain Model} for a bean.
 *  
 * @author Xavier DURY
 * @param <B> The type of the bean.
 */
public final class BeanModel<B> implements Model<B>, Iterable<Property<? super B, ?>> {
	
	private final Map<String, Property<? super B, ?>> properties;
	
	BeanModel(Map<String, Property<? super B, ?>> properties) {
		this.properties = requireNonNull(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <R> R accept(ModelVisitor<? extends B, ? extends R> visitor) {
		return requireNonNull(visitor).visitBean(this);
	}
	
	/**
	 * Add a property and return a <strong>new</strong> <code>BeanModel</code>.
	 * Properties order will be retained to be used for comparison/sorting.
	 * There are 2 reasons to return a new <code>BeanModel</code>.
	 * <ol>
	 * <li>To guarantee immutability, a property added later won't have any effects on previously built <code>BeanModel</code>s.</li>
	 * <li>To allow the type <code>&lt;B&gt;</code> to be <em>upgraded</em> as a new type <code>&lt;C extends B&gt;</code>.</li>
	 * </ol>
	 * Example:
	 * <pre> 
	 * public interface Shape {
	 *     Color getColor();
	 *     double getSurface();
	 * }
	 * 
	 * public interface Rectangle extends Shape {
	 *     double getWidth();
	 *     double getHeight();
	 * }
	 * 
	 * public interface Circle extends Shape {
	 *     double getRadius();
	 * }
	 * 
	 * Model&lt;Circle&gt; model = Model.bean()
	 *     .addProperty("hashcode", Object::hashcode, Model.value()) // this would yield a BeanModel&lt;Object&gt;
	 *     .addProperty("color", Shape::getColor, Model.value()) // this new bean is now a BeanModel&lt;Shape&gt;
	 *     .addProperty("radius", Circle::getRadius, Model.value()) // this gives a BeanModel&lt;Circle&gt;
	 *     .addProperty("surface", Shape::getSurface, Model.value()) // this is still a BeanModel&lt;Circle&gt;
	 *     .addProperty("width", Rectangle::getWidth, Model.value()); // will give a compilation error as Rectangle does not extend Circle nor is a superclass
	 * </pre>
	 * @param name The name of the property.
	 * @param extractor The getter of the property or any function that can extract the property from the bean.
	 * @param delegate The delegate {@linkplain Model} for that property.
	 * @return A <strong>new</strong> <code>BeanModel</code> instance with the added property.
	 * @param <C> The potentially upgraded type of the <code>BeanModel</code>.
	 * @param <P> The type of the property.
	 */
	public <C extends B, P> BeanModel<C> addProperty(String name, Function<? super C, ? extends P> extractor, Model<? super P> delegate) {
		Map<String, Property<? super C, ?>> properties = new LinkedHashMap<>(this.properties);
		properties.put(requireNonNull(name), new Property<>(name, extractor, delegate));
		return new BeanModel<>(properties);
	}

	/**
	 * Get the properties which are registered for this <code>BeanModel</code>.
	 * 
	 * @return The properties.
	 */
	public Collection<Property<? super B, ?>> getProperties() {
		return unmodifiableCollection(this.properties.values());
	}

	/**
	 * Get a property by its name.
	 * 
	 * @param name The name of the property.
	 * @return The corresponding property or null.
	 */
	public <P> Property<? super B, P> getProperty(String name) {
		@SuppressWarnings("unchecked")
		Property<? super B, P> property = (Property<? super B, P>) properties.get(requireNonNull(name));
		return property;
	}
	
	/**
	 * Iterate over all properties.
	 * 
	 * @return An iterator of all properties.
	 */
	@Override
	public Iterator<Property<? super B, ?>> iterator() {
		return getProperties().iterator();
	}
}
