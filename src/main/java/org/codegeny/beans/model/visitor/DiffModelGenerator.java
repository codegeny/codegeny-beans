package org.codegeny.beans.model.visitor;

import static org.codegeny.beans.model.Model.map;
import static org.codegeny.beans.model.Model.set;
import static org.codegeny.beans.model.Model.value;

import java.util.HashSet;

import org.codegeny.beans.diff.BeanDiff;
import org.codegeny.beans.diff.Diff;
import org.codegeny.beans.diff.ListDiff;
import org.codegeny.beans.diff.MapDiff;
import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.Model;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.Property;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class DiffModelGenerator<T> implements ModelVisitor<T, Model<Diff<T>>> {
		
	public Model<Diff<T>> visitBean(BeanModel<? super T> bean) {
		return bean.getProperties().stream().reduce(Model.<Diff<T>> bean(), (b, p) -> visitProperty(b, p), (a, b) -> a);
	}

	private @SuppressWarnings("unchecked") <P> BeanModel<Diff<T>> visitProperty(BeanModel<Diff<T>> bean, Property<? super T, P> property) {
		return bean.addProperty(property.getName(), b -> (Diff<P>) ((BeanDiff<T>) b).getProperties().get(property.getName()), property.acceptDelegate(new DiffModelGenerator<>()));
	}
	
	public @SuppressWarnings("unchecked") <K, V> Model<Diff<T>> visitMap(MapModel<? super T, K, V> map) {
		return map(map.getKeyDelegate(), map.acceptValueDelegate(new DiffModelGenerator<>()), m -> ((MapDiff<T, K, V>) m).getMap());
	}

	public Model<Diff<T>> visitValue(ValueModel<? super T> value) {
		return Model.<Diff<T>> bean()
			.addProperty("score", Diff::getScore, value())
			.addProperty("status", Diff::getStatus, value())
			.addProperty("left", Diff::getLeft, value)
			.addProperty("right", Diff::getRight, value);
	}
	
	public @SuppressWarnings("unchecked") <E> Model<Diff<T>> visitSet(SetModel<? super T, E> collection) {
		return set(collection.acceptDelegate(new DiffModelGenerator<>()), l -> new HashSet<>(((ListDiff<T, E>) l).getList()));
	}
	
	public @SuppressWarnings("unchecked") <E> Model<Diff<T>> visitList(ListModel<? super T, E> collection) {
		return set(collection.acceptDelegate(new DiffModelGenerator<>()), l -> new HashSet<>(((ListDiff<T, E>) l).getList()));
	}
}