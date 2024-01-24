package itkach.aard2;

public interface LookupListener {
    void onLookupStarted(String query);
    void onLookupFinished(String query);
    void onLookupCanceled(String query);
}
