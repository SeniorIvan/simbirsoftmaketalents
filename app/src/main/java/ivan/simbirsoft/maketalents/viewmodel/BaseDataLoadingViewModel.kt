package ivan.simbirsoft.maketalents.viewmodel

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Ivan Kuznetsov
 * 16.08.17.
 */

interface BaseDataLoadingViewModelInputs {

    /** Call when the repeat button has been clicked. */
    fun repeatButtonClicked()
}

interface BaseDataLoadingViewModelOutputs<ItemType> {

    /** Emits when data has been  loaded. */
    fun data(): Observable<ItemType>

    /** Emits when we should change page in {@link ViewAnimator}. */
    fun currentContentState(): Observable<ViewModelContentState>

    fun contentErrorText(): Observable<String>
}

abstract class BaseDataLoadingViewModel<ItemType> : BaseViewModel(),
        BaseDataLoadingViewModelInputs, BaseDataLoadingViewModelOutputs<ItemType> {

    private val mDataObservable = BehaviorSubject.create<ItemType>()
    private val mCurrentContentStateObservable =
            BehaviorSubject.createDefault(ViewModelContentState.LOADING_PAGE)
    private val mContentErrorTextObservable = BehaviorSubject.create<String>()

    ///////////

    protected var mData: ItemType? = null
    private var mDataDisposable: Disposable? = null
    private var mCurrentContentPage = mCurrentContentStateObservable.value

    fun baseInputs(): BaseDataLoadingViewModelInputs {
        return this
    }

    fun baseOutputs(): BaseDataLoadingViewModelOutputs<ItemType> {
        return this
    }

    override fun onCreated() {
        super.onCreated()
        fetchData()
    }

    protected abstract fun onCreateDataObservable(): Observable<ItemType>

    protected open fun onContentStateWasChanged(state: ViewModelContentState) {}

    override fun onCleared() {
        super.onCleared()
        mDataDisposable?.dispose()
    }

    override fun repeatButtonClicked() {
        updateContentPageAndEmit(ViewModelContentState.LOADING_PAGE)
        fetchData()
    }

    override fun data(): Observable<ItemType> = mDataObservable

    override fun currentContentState(): Observable<ViewModelContentState> =
            mCurrentContentStateObservable

    override fun contentErrorText(): Observable<String> = mContentErrorTextObservable

    private fun updateContentPageAndEmit(newState: ViewModelContentState) {
        if (mCurrentContentPage == newState) {
            return
        }
        mCurrentContentPage = newState
        mCurrentContentStateObservable.onNext(newState)
        onContentStateWasChanged(newState)
    }

    private fun emitErrorMessageAndContentState(error: ViewModelError) {
        emitErrorText(error.message())
        updateContentPageAndEmit(ViewModelContentState.ERROR_PAGE)
    }

    protected fun fetchData() {
        mDataDisposable?.dispose()

        mDataDisposable = onCreateDataObservable().
                observeOn(AndroidSchedulers.mainThread()).
                subscribeOn(Schedulers.io()).subscribe({ data ->
            if (data == null) {
                updateContentPageAndEmit(ViewModelContentState.EMPTY_DATA_PAGE)
                return@subscribe
            }

            mData = data
            mDataObservable.onNext(mData!!)
            updateContentPageAndEmit(ViewModelContentState.DATA_PAGE)
        }, { throwable ->
            if (throwable is ViewModelThrowable) {
                emitErrorMessageAndContentState(throwable.error)
            } else {
                emitErrorMessageAndContentState(CommonError.SOME_ERROR)
            }
            updateContentPageAndEmit(ViewModelContentState.ERROR_PAGE)
        })
    }

    private fun emitErrorText(text: String) {
        mContentErrorTextObservable.onNext(text)
    }
}