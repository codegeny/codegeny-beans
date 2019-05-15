package org.codegeny.beans.model.visitor;

/*-
 * #%L
 * codegeny-beans
 * %%
 * Copyright (C) 2016 - 2018 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
	
	public StringBuilder visitBean(BeanModel<T> bean) {
		this.builder.append("BeanModel {");
		int count = forEachIndexed(bean.getProperties(), (i, p) -> p.accept(new DescribeModelVisitor<>(this.builder.append(i > 0 ? "," : "").append("\n").append(this.indent).append("  ").append(p.getName()).append(": "), this.indent.concat("  "))));
		return this.builder.append(count > 0 ? "\n" : "").append(this.indent).append("}"); 
	}
	
	public <K, V> StringBuilder visitMap(MapModel<T, K, V> map) {
		this.builder.append("Map {");
		this.builder.append("\n").append(this.indent).append("  ").append("key: ");
		map.acceptKey(new DescribeModelVisitor<>(this.builder, this.indent.concat("  ")));
		this.builder.append(",\n").append(this.indent).append("  ").append("value: ");
		map.acceptValue(new DescribeModelVisitor<>(this.builder, this.indent.concat("  ")));
		return this.builder.append("\n").append(this.indent).append("}"); 
	}
	
	public StringBuilder visitValue(ValueModel<T> value) {
		return this.builder.append("Value");
	}
	
	public <E> StringBuilder visitSet(SetModel<T, E> values) {
		this.builder.append("Set [");
		values.acceptElement(new DescribeModelVisitor<>(this.builder, this.indent));
		return this.builder.append("]");
	}
	
	public <E> StringBuilder visitList(ListModel<T, E> values) {
		this.builder.append("List [");
		values.acceptElement(new DescribeModelVisitor<>(this.builder, this.indent));
		return this.builder.append("]");
	}
}
