package org.codegeny.beans.model.visitor;

import static org.codegeny.beans.util.IndexedConsumer.forEachIndexed;

import org.codegeny.beans.model.BeanModel;
import org.codegeny.beans.model.ListModel;
import org.codegeny.beans.model.MapModel;
import org.codegeny.beans.model.ModelVisitor;
import org.codegeny.beans.model.SetModel;
import org.codegeny.beans.model.ValueModel;

public class DescribeModelVisitor<T> implements ModelVisitor<T, StringBuilder> {
	
	private final StringBuilder builder;
	private final String indent;
		
	public DescribeModelVisitor() {
		this(new StringBuilder(), "");
	}
	
	private DescribeModelVisitor(StringBuilder builder, String indent) {
		this.builder = builder;
		this.indent = indent;
	}
	
	public StringBuilder visitBean(BeanModel<? super T> bean) {
		this.builder.append("Bean {");
		int count = forEachIndexed(bean.getProperties(), (i, p) -> {
			this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(p.getName()).append(": ");
			p.acceptDelegate(new DescribeModelVisitor<>(this.builder, this.indent.concat("  ")));
		});
		return this.builder.append(count > 0 ? "\n" : "").append(this.indent).append("}"); 
	}
	
	public <K, V> StringBuilder visitMap(MapModel<? super T, K, V> map) {
		this.builder.append("Map {");
		this.builder.append("\n").append(this.indent).append("  ").append("key: ");
		map.acceptKeyDelegate(new DescribeModelVisitor<>(this.builder, this.indent.concat("  ")));
		this.builder.append(",\n").append(this.indent).append("  ").append("value: ");
		map.acceptValueDelegate(new DescribeModelVisitor<>(this.builder, this.indent.concat("  ")));
		return this.builder.append("\n").append(this.indent).append("}"); 
	}
	
	public StringBuilder visitValue(ValueModel<? super T> value) {
		return this.builder.append("Value");
	}
	
	public <E> StringBuilder visitSet(SetModel<? super T, E> values) {
		this.builder.append("Set [");
		values.acceptDelegate(new DescribeModelVisitor<>(this.builder, this.indent));
		return this.builder.append("]");
	}
	
	public <E> StringBuilder visitList(ListModel<? super T, E> values) {
		this.builder.append("List [");
		values.acceptDelegate(new DescribeModelVisitor<>(this.builder, this.indent));
		return this.builder.append("]");
	}
}