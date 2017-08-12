package org.codegeny.beans.model;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.codegeny.beans.model.visitor.CompareModelVisitor;
import org.codegeny.beans.model.visitor.DescribeModelVisitor;
import org.codegeny.beans.model.visitor.HashModelVisitor;
import org.codegeny.beans.model.visitor.PathModelVisitor;
import org.codegeny.beans.model.visitor.ToStringModelVisitor;
import org.codegeny.beans.path.Path;
import org.codegeny.beans.util.Hasher;

/**
 * Base interface for modeling an object structure as a tree.
 * Models can be viewed as nodes from a tree.
 * A model by itself cannot do much but it can be visited by {@link ModelVisitor}s which can implement complex logic.
 * There are currently different type of models/nodes:
 * <ul>
 * <li>{@link BeanModel} which represents a bean.</li>
 * <li>{@link ValueModel} which represents a terminal value (leaf). Generally atomic types like Numbers, Strings or simple value objects.</li>
 * <li>{@link SetModel} which represents a collection of objects.</li>
 * <li>{@link MapModel} which represents a map.</li>
 * </ul>
 * All implementations are required to be thread-safe.
 * 
 * @author Xavier DURY
 * @param <T> The type of object this model represent.
 */
public interface Model<T> extends Comparator<T> {
	
	/**
	 * Construct a new empty {@link BeanModel}.
	 * 
	 * @return The bean model.
	 * @param <B> The bean type.
	 */
	static <B> BeanModel<B> bean() {
		return new BeanModel<>(emptyMap());
	}

	/**
	 * Construct a new {@link ListModel} for a list of &lt;E&gt; elements which implements the {@link List} interface. 
	 * 
	 * @param delegate The delegate {@link Model} to be used for elements.
	 * @return The list model.
	 * @param <C> The type of the list of &lt;E&gt; elements.
	 * @param <E> The type of elements.
	 */
	static <C extends List<? extends E>, E> ListModel<C, E> list(Model<? super E> delegate) {
		return list(delegate, identity());
	}
	
	/**
	 * Construct a new {@link ListModel} for a list of &lt;E&gt; objects which does not implement the {@link List} interface. 
	 * 
	 * @param delegate The delegate {@link Model} to be used for elements.
	 * @param extractor The collector is a function which transform objects of type &lt;C&gt; to a <code>List&lt;E&gt;</code>.
	 * @return The list model.
	 * @param <C> The type of the list of &lt;E&gt; elements.
	 * @param <E> The type of elements.
	 */
	static <C, E> ListModel<C, E> list(Model<? super E> delegate, Function<? super C, ? extends List<? extends E>> extractor) {
		return new ListModel<>(extractor, delegate);
	}
	
	/**
	 * Construct a new {@link MapModel} for a map of &lt;K, V&gt; entries which implements the {@link Map} interface. 
	 * 
	 * @param keyDelegate The delegate {@link Model} to be used for keys.
	 * @param valueDelegate The delegate {@link Model} to be used for values.
	 * @return The map model.
	 * @param <M> The type of the map of &lt;K, V&gt; entries.
	 * @param <K> The type of keys.
	 * @param <V> The type of values.
	 */
	static <M extends Map<? extends K, ? extends V>, K, V> MapModel<M, K, V> map(Model<? super K> keyDelegate, Model<? super V> valueDelegate) {
		return map(keyDelegate, valueDelegate, identity());
	}
	
	/**
	 * Construct a new {@link MapModel} for a map of &lt;K, V&gt; entries which does not implement the {@link Map} interface. 
	 * 
	 * @param keyDelegate The delegate {@link Model} to be used for keys.
	 * @param valueDelegate The delegate {@link Model} to be used for values.
	 * @param extractor The collector is a function which transform objects of type &lt;M&gt; to a <code>Map&lt;K, V&gt;</code>.
	 * @return The map model.
	 * @param <M> The type of the map of &lt;K, V&gt; entries.
	 * @param <K> The type of keys.
	 * @param <V> The type of values.
	 */
	static <M, K, V> MapModel<M, K, V> map(Model<? super K> keyDelegate, Model<? super V> valueDelegate, Function<? super M, ? extends Map<? extends K, ? extends V>> extractor) {
		return new MapModel<>(extractor, keyDelegate, valueDelegate);
	}
	
	/**
	 * Construct a new {@link SetModel} for a set of &lt;E&gt; elements which implements the {@link Set} interface.
	 * 
	 * @param delegate The delegate {@link Model} to be used for elements.
	 * @return The set model.
	 * @param <C> The type of the set of &lt;E&gt; elements.
	 * @param <E> The type of elements.
	 */
	static <C extends Set<? extends E>, E> SetModel<C, E> set(Model<? super E> delegate) {
		return set(delegate, identity());
	}
	
	/**
	 * Construct a new {@link SetModel} for a set of &lt;E&gt; objects which does not implement the {@link Set} interface. 
	 * 
	 * @param delegate The delegate {@link Model} to be used for elements.
	 * @param extractor The collector is a function which transform objects of type &lt;C&gt; to a <code>Collection&lt;E&gt;</code>.
	 * @return The set model.
	 * @param <C> The type of the set of &lt;E&gt; elements.
	 * @param <E> The type of elements.
	 */
	static <C, E> SetModel<C, E> set(Model<? super E> delegate, Function<? super C, ? extends Set<? extends E>> extractor) {
		return new SetModel<>(extractor, delegate);
	}
	
	/**
	 * Construct a new {@link ValueModel} for an object which implements the {@link Comparable} interface.
	 *  
	 * @return The value model.
	 * @param <T> The type of the value which must be comparable.
	 */
	static <T extends Comparable<? super T>> ValueModel<T> value() {
		return value(naturalOrder());
	}
	
	/**
	 * Construct a new {@link ValueModel} for an object which does not implement the {@link Comparable} interface.
	 * 
	 * @param comparator The {@link Comparator} to be used to compare objects of type &lt;T&gt;
	 * @return The value model.
	 * @param <T> The type of the value.
	 */
	static <T> ValueModel<T> value(Comparator<? super T> comparator) {
		return new ValueModel<>(comparator);
	}
	
	/**
	 * Accept a {@link ModelVisitor} and return some result.
	 * 
	 * @param visitor The model visitor.
	 * @return The result of the visitor computation.
	 * @param <R> The type of the result.
	 */
	<R> R accept(ModelVisitor<? extends T, ? extends R> visitor);
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	default int compare(T left, T right) {
		return accept(new CompareModelVisitor<>(left, right));
	}
	
	/**
	 * Create a string representation of the model.
	 * 
	 * @return A string representation for the model.
	 */
	default String describe() {
		return accept(new DescribeModelVisitor<>()).toString();
	}
	
	/**
	 * Extract the given path from the target.
	 * 
	 * @param target The target.
	 * @param path The path.
	 * @return The extracted value according to path.
	 */
	default Object extract(T target, Path path) {
		return accept(new PathModelVisitor<>(target, path.elements().iterator()));
	}
	
	/**
	 * Hash the given target using the given {@link Hasher}
	 * 
	 * @param target The target to hash.
	 * @param hasher The hasher.
	 * @return The hash result.
	 */
	default int hash(T target, Hasher hasher) {
		return accept(new HashModelVisitor<>(target, hasher)).toHash();
	}
	
	/**
	 * Create a string representation for the given target.
	 * 
	 * @param target The target object.
	 * @return A string representation for that object.
	 */
	default String toString(T target) {
		return accept(new ToStringModelVisitor<>(target)).toString();
	}
}