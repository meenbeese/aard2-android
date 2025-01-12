package itkach.aard2;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.text.StringSearch;

import java.text.StringCharacterIterator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import itkach.slob.Slob;

final class BlobDescriptorList extends AbstractList<BlobDescriptor> {

    private final String TAG = getClass().getSimpleName();
    enum SortOrder {
        TIME, NAME
    }
    private final Application app;
    private final DescriptorStore<BlobDescriptor> store;
    private final List<BlobDescriptor> list;
    private final List<BlobDescriptor> filteredList;
    private String filter;
    private SortOrder order;
    private boolean ascending;
    private final DataSetObservable dataSetObservable;
    private final Comparator<BlobDescriptor> nameComparatorAsc;
    private final Comparator<BlobDescriptor> nameComparatorDesc;
    private final Comparator<BlobDescriptor> timeComparatorAsc;
    private final Comparator<BlobDescriptor> timeComparatorDesc;
    private Comparator<BlobDescriptor> comparator;
    private final Comparator<BlobDescriptor> lastAccessComparator;
    private final Slob.KeyComparator keyComparator;
    private final int maxSize;
    private final RuleBasedCollator filterCollator;
    private final Handler handler;

    BlobDescriptorList(Application app, DescriptorStore<BlobDescriptor> store) {
        this(app, store, 100);
    }

    BlobDescriptorList(Application app, DescriptorStore<BlobDescriptor> store, int maxSize) {
        this.app = app;
        this.store = store;
        this.maxSize = maxSize;
        this.list = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.dataSetObservable = new DataSetObservable();
        this.filter = "";

        keyComparator = Slob.Strength.QUATERNARY.comparator;
        nameComparatorAsc = (b1, b2) -> keyComparator.compare(b1.key, b2.key);
        nameComparatorDesc = Collections.reverseOrder(nameComparatorAsc);
        timeComparatorAsc = (b1, b2) -> Utility.INSTANCE.compare(b1.createdAt, b2.createdAt);
        timeComparatorDesc = Collections.reverseOrder(timeComparatorAsc);
        lastAccessComparator = (b1, b2) -> Utility.INSTANCE.compare(b2.lastAccess, b1.lastAccess);

        order = SortOrder.TIME;
        ascending = false;
        setSort(order, false);

        try {
            filterCollator = (RuleBasedCollator) Collator.getInstance(Locale.ROOT).clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        filterCollator.setStrength(Collator.PRIMARY);
        filterCollator.setAlternateHandlingShifted(false);
        handler = new Handler(Looper.getMainLooper());
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.dataSetObservable.registerObserver(observer);
    }

    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        this.filteredList.clear();
        if (filter == null || filter.length() == 0) {
            this.filteredList.addAll(this.list);
        }
        else {
            for (BlobDescriptor bd : this.list) {
                StringSearch stringSearch = new StringSearch(
                        filter, new StringCharacterIterator(bd.key), filterCollator);
                int matchPos = stringSearch.first();
                if (matchPos != StringSearch.DONE) {
                    this.filteredList.add(bd);
                }
            }
        }
        sortOrderChanged();
    }

    private void sortOrderChanged() {
        Utility.INSTANCE.sort(this.filteredList, comparator);
        this.dataSetObservable.notifyChanged();
    }

    void load() {
        this.list.addAll(this.store.load(BlobDescriptor.class));
        notifyDataSetChanged();
    }

    private void doUpdateLastAccess(BlobDescriptor bd) {
        long t = System.currentTimeMillis();
        long dt = t - bd.lastAccess;
        if (dt < 2000) {
            return;
        }
        bd.lastAccess = t;
        store.save(bd);
    }

    void updateLastAccess(final BlobDescriptor bd) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doUpdateLastAccess(bd);
        }
        else {
            handler.post(() -> doUpdateLastAccess(bd));
        }
    }

    Slob resolveOwner(BlobDescriptor bd) {
        Slob slob = app.getSlob(bd.slobId);
        if (slob == null) {
            slob = app.findSlob(bd.slobUri);
        }
        return slob;
    }

    Slob.Blob resolve(BlobDescriptor bd) {
        Slob slob = resolveOwner(bd);
        Slob.Blob blob = null;
        if (slob == null) {
            return null;
        }
        String slobId = slob.getId().toString();
        if (slobId.equals(bd.slobId)) {
            blob = new Slob.Blob(slob, bd.blobId, bd.key, bd.fragment);
        } else {
            try {
                Iterator<Slob.Blob> result = slob.find(bd.key,
                        Slob.Strength.QUATERNARY);
                if (result.hasNext()) {
                    blob = result.next();
                    bd.slobId = slobId;
                    bd.blobId = blob.id;
                }
            }
            catch (Exception ex) {
                Log.w(TAG, String.format("Failed to resolve descriptor %s (%s) in %s (%s)",
                              bd.blobId, bd.key, slob.getId(), slob.fileURI), ex);
            }
        }
        if (blob != null) {
            updateLastAccess(bd);
        }
        return blob;
    }

    public BlobDescriptor createDescriptor(String contentUrl) {
        Log.d(TAG, "Create descriptor from content url: " + contentUrl);
        Uri uri = Uri.parse(contentUrl);
        BlobDescriptor bd = BlobDescriptor.fromUri(uri);
        if (bd != null) {
            String slobUri = app.getSlobURI(bd.slobId);
            Log.d(TAG, "Found slob uri for: " + bd.slobId + " " + slobUri);
            bd.slobUri = slobUri;
        }
        return bd;
    }

    public BlobDescriptor add(String contentUrl) {
        BlobDescriptor bd = createDescriptor(contentUrl);
        int index = this.list.indexOf(bd);
        if (index > -1) {
            return this.list.get(index);
        }
        this.list.add(bd);
        store.save(bd);
        if (this.list.size() > this.maxSize) {
            Utility.INSTANCE.sort(this.list, lastAccessComparator);
            BlobDescriptor lru = this.list.remove(this.list.size() - 1);
            store.delete(lru.id);
        }
        notifyDataSetChanged();
        return bd;
    }

    public void remove(String contentUrl) {
        int index = this.list.indexOf(createDescriptor(contentUrl));
        if (index > -1) {
            removeByIndex(index);
        }
    }

    private void removeByIndex(int index) {
        BlobDescriptor bd = this.list.remove(index);
        if (bd != null) {
            boolean removed = store.delete(bd.id);
            Log.d(TAG, String.format("Item (%s) %s removed? %s", bd.key, bd.id, removed));
            if (removed) {
                notifyDataSetChanged();
            }
        }
    }

    public boolean contains(String contentUrl) {
        BlobDescriptor toFind = createDescriptor(contentUrl);
        for (BlobDescriptor bd : this.list) {
            if (bd.equals(toFind)) {
                Log.d(TAG, "Found exact match, bookmarked");
                return true;
            }
            assert bd.key != null;
            assert bd.slobUri != null;
            if (bd.key.equals(toFind.key) && bd.slobUri.equals(toFind.slobUri)) {
                Log.d(TAG, "Found approximate match, bookmarked");
                return true;
            }
        }
        Log.d(TAG, "not bookmarked");
        return false;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        notifyDataSetChanged();
    }

    public String getFilter() {
        return this.filter;
    }

    @Override
    public BlobDescriptor get(int location) {
        return this.filteredList.get(location);
    }

    @Override
    public int size() {
        return this.filteredList.size();
    }

    public void setSort(boolean ascending) {
        setSort(this.order, ascending);
    }

    public void setSort(SortOrder order) {
        setSort(order, this.ascending);
    }

    public SortOrder getSortOrder() {
        return this.order;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    public void setSort(SortOrder order, boolean ascending) {
        this.order = order;
        this.ascending = ascending;
        Comparator<BlobDescriptor> c = null;
        if (order == SortOrder.NAME) {
            c = ascending ? nameComparatorAsc : nameComparatorDesc;
        }
        if (order == SortOrder.TIME) {
            c = ascending ? timeComparatorAsc : timeComparatorDesc;
        }
        if (c != comparator) {
            comparator = c;
            sortOrderChanged();
        }
    }
}
