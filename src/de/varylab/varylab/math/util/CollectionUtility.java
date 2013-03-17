package de.varylab.varylab.math.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class CollectionUtility {

	public static <	T, L extends Collection<T> >
	Collection< Collection<T> > subsets(Collection<T> collection, int k) {
		if(k > collection.size() || k < 0) {
			return Collections.emptyList();
		}
		if(k == 0) {
			Collection<Collection<T>> LL = new LinkedList<Collection<T>>();
			LL.add(new LinkedList<T>());
			return LL; 
		}
		if(k == collection.size()) {
			LinkedList<Collection<T>> LL = new LinkedList< Collection<T> >();
			LL.add(new LinkedList<T>(collection));
			return LL;
		}
		Collection<T> LC = new LinkedList<T>(collection);
		T first = LC.iterator().next();
		LC.remove(first);
		Collection<Collection<T>> LL = subsets(LC,k);
	
		for(Collection<T> Lk1 : subsets(LC,k-1)) {
			Lk1.add(first);
			LL.add(Lk1);
		}
		return LL;
	}
}
