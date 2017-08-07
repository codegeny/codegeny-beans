package org.codegeny.beans.path;

import java.io.Serializable;

public interface PathElement extends Serializable {

	<R> R accept(R parent, PathVisitor<R> visitor);
}
