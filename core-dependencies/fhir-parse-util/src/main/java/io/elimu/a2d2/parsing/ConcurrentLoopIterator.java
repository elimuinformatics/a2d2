// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.elimu.a2d2.parsing;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ConcurrentLoopIterator<E> implements Iterator<E> {

	private final Collection<E> col;
	private Iterator<E> iterator;

	ConcurrentLoopIterator(Collection<E> col) {
		this.col = col;
		reset();
	}

	@Override
	public boolean hasNext() {
		return col.size() != 0;
	}

	@Override
    public synchronized E next() {
        if (col.size() == 0) {
            throw new NoSuchElementException("There are no elements for this iterator to loop on");
        }
        if (iterator.hasNext() == false) {
            reset();
        }
        return iterator.next();
    }

	@Override
    public void remove() {
		//do nothing. Iterator works on static list
    }

    /**
     * Resets the iterator back to the start of the collection.
     */
    public void reset() {
        iterator = col.iterator();
    }
}
