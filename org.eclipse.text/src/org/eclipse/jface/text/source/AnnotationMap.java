/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Internal implementation of {@link org.eclipse.jface.text.source.IAnnotationMap}.
 *
 * @since 3.0
 */
class AnnotationMap implements IAnnotationMap {

    /**
     * The lock object used to synchronize the operations explicitly defined by
     * <code>IAnnotationMap</code>
     */
    private Object fLockObject;
    /**
     * The internal lock object used if <code>fLockObject</code> is <code>null</code>.
	 * @since 3.2
	 */
    private final Object fInternalLockObject= new Object();

    /** The map holding the annotations */
    private Map fInternalMap;

    /**
     * Creates a new annotation map with the given capacity.
     *
     * @param capacity the capacity
     */
    public AnnotationMap(int capacity) {
        fInternalMap= new HashMap(capacity);
    }

    @Override
	public synchronized void setLockObject(Object lockObject) {
        fLockObject= lockObject;
    }

    @Override
	public synchronized Object getLockObject() {
        if (fLockObject == null)
        	return fInternalLockObject;
        return fLockObject;
    }

    @Override
	public Iterator valuesIterator() {
        synchronized (getLockObject()) {
            return new ArrayList(fInternalMap.values()).iterator();
        }
    }

    @Override
	public Iterator keySetIterator() {
        synchronized (getLockObject()) {
            return new ArrayList(fInternalMap.keySet()).iterator();
        }
    }

    @Override
	public boolean containsKey(Object annotation) {
        synchronized (getLockObject()) {
            return fInternalMap.containsKey(annotation);
        }
    }

    @Override
	public Object put(Object annotation, Object position) {
        synchronized (getLockObject()) {
            return fInternalMap.put(annotation, position);
        }
    }

    @Override
	public Object get(Object annotation) {
        synchronized (getLockObject()) {
            return fInternalMap.get(annotation);
        }
    }

    @Override
	public void clear() {
        synchronized (getLockObject()) {
            fInternalMap.clear();
        }
    }

    @Override
	public Object remove(Object annotation) {
        synchronized (getLockObject()) {
            return fInternalMap.remove(annotation);
        }
    }

    @Override
	public int size() {
        synchronized (getLockObject()) {
            return fInternalMap.size();
        }
    }

    @Override
	public boolean isEmpty() {
        synchronized (getLockObject()) {
			return fInternalMap.isEmpty();
		}
    }

	@Override
	public boolean containsValue(Object value) {
		synchronized(getLockObject()) {
			return fInternalMap.containsValue(value);
		}
	}

	@Override
	public void putAll(Map map) {
		synchronized (getLockObject()) {
			fInternalMap.putAll(map);
		}
	}

	@Override
	public Set entrySet() {
		synchronized (getLockObject()) {
			return fInternalMap.entrySet();
		}
	}

	@Override
	public Set keySet() {
		synchronized (getLockObject()) {
			return fInternalMap.keySet();
		}
	}

	@Override
	public Collection values() {
		synchronized (getLockObject()) {
			return fInternalMap.values();
		}
	}
}
