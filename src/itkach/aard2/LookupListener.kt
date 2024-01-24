package itkach.aard2

interface LookupListener {
    fun onLookupStarted(query: String?)
    fun onLookupFinished(query: String?)
    fun onLookupCanceled(query: String?)
}
