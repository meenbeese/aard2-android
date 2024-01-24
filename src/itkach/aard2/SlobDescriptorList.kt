package itkach.aard2

import java.util.Comparator

import itkach.slob.Slob

internal class SlobDescriptorList(
    private val app: Application,
    store: DescriptorStore<SlobDescriptor>
) : BaseDescriptorList<SlobDescriptor>(SlobDescriptor::class.java, store) {

    private val comparator = Comparator<SlobDescriptor> { d1, d2 ->
        // Dictionaries that are unfavorited
        // Go immediately after favorites
        if (d1.priority == 0L && d2.priority == 0L) {
            Utility.compare(d2.lastAccess, d1.lastAccess)
        }
        // Favorites are always above other
        else if (d1.priority == 0L && d2.priority > 0L) {
            1
        }
        else if (d1.priority > 0L && d2.priority == 0L) {
            -1
        }
        // Old favorites are above more recent ones
        else {
            Utility.compare(d1.priority, d2.priority)
        }
    }

    fun resolve(sd: SlobDescriptor): Slob {
        return app.getSlob(sd.id)
    }

    fun sort() {
        Utility.sort(this, comparator)
    }

    override fun load() {
        beginUpdate()
        super.load()
        sort()
        endUpdate(true)
    }
}
