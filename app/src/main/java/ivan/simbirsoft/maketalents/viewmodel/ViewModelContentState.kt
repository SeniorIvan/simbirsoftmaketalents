package ivan.simbirsoft.maketalents.viewmodel

/**
 * Created by Ivan Kuznetsov
 * 29.10.2017.
 */

/**
 * [index] index of child in ViewAnimator
 */
enum class ViewModelContentState(val index: Int) {
    DATA_PAGE(0),
    LOADING_PAGE(1),
    ERROR_PAGE(2),
    EMPTY_DATA_PAGE(3)
}