package org.codegeny.beans.model.visitor;

import org.codegeny.beans.model.Model;

public interface Typer<S> {
	
	Typer<Object> IDENTITY = new Typer<Object>() {
		
		@Override
		@SuppressWarnings("unchecked")
		public <T> T retype(Model<T> model, Object value) {
			return (T) value;
		}
	};
	
	<T> T retype(Model<T> model, S value);
}