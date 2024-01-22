package itkach.aard2;

import java.util.Comparator;

import itkach.slob.Slob;

public class SlobDescriptorList extends BaseDescriptorList<SlobDescriptor> {

    private final Application            app;
    private Comparator<SlobDescriptor>   comparator;

    SlobDescriptorList(Application app, DescriptorStore<SlobDescriptor> store) {
        super(SlobDescriptor.class, store);
        this.app = app;
        comparator = (d1, d2) -> {
            // Dictionaries that are unfavorited
            // Go immediately after favorites
            if (d1.priority == 0 && d2.priority == 0) {
                return Util.compare(d2.lastAccess, d1.lastAccess);
            }
            // Favorites are always above other
            if (d1.priority == 0 && d2.priority > 0) {
                return 1;
            }
            if (d1.priority > 0 && d2.priority == 0) {
                return -1;
            }
            // Old favorites are above more recent ones
            return Util.compare(d1.priority, d2.priority);
        };
    }

    Slob resolve(SlobDescriptor sd) {
        return this.app.getSlob(sd.id);
    }

    void sort() {
        Util.sort(this, comparator);
    }

    @Override
    void load() {
        beginUpdate();
        super.load();
        sort();
        endUpdate(true);
    }
}
