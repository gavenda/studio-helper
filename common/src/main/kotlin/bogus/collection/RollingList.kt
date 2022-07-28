package bogus.collection

class RollingList<E>(val maxSize: Int = 10) : MutableList<E> {
    val list = mutableListOf<E>()

    override val size: Int
        get() = list.size

    override fun get(index: Int): E = list[index]

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): MutableIterator<E> = list.iterator()

    override fun listIterator(): MutableListIterator<E> = list.listIterator()

    override fun listIterator(index: Int): MutableListIterator<E> = list.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = list.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: E): Int = list.lastIndexOf(element)

    override fun indexOf(element: E): Int = list.indexOf(element)

    override fun containsAll(elements: Collection<E>): Boolean = list.containsAll(elements)

    override fun contains(element: E): Boolean = list.contains(element)

    override fun add(element: E): Boolean {
        if (list.size == maxSize) {
            list.removeFirst()
        }

        return list.add(element)
    }

    override fun add(index: Int, element: E) =
        throw UnsupportedOperationException("Cannot add an element with index")

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        throw UnsupportedOperationException("Cannot add an element with index")

    override fun addAll(elements: Collection<E>): Boolean {
        var changed = false
        elements.forEach { changed = changed || add(it) }
        return changed
    }

    override fun clear() = list.clear()

    override fun remove(element: E): Boolean =
        throw UnsupportedOperationException("Remove not supported")

    override fun removeAll(elements: Collection<E>): Boolean =
        throw UnsupportedOperationException("Remove not supported")

    override fun removeAt(index: Int): E =
        throw UnsupportedOperationException("Remove not supported")

    override fun retainAll(elements: Collection<E>): Boolean = list.retainAll(elements)

    override fun set(index: Int, element: E): E =
        throw UnsupportedOperationException("Cannot set an element with index")
}