package ivan.simbirsoft.maketalents.activities.base

import android.os.Bundle
import ivan.simbirsoft.maketalents.viewmodel.BaseViewModel
import ivan.simbirsoft.maketalents.viewmodel.ViewModelLifecycleDispatcher
import ivan.simbirsoft.maketalents.viewmodel.ViewModelUtils

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
@Suppress("UNCHECKED_CAST")
abstract class ViewModelActivity<VM : BaseViewModel>: BaseActivity() {

    protected lateinit var viewModel: VM

    // TODO не совсем удачное название, по названию это как будто бы коллбек, который сработает
    // TODO при создании вью модели, а на самом деле - метод, который её возвращает
    abstract fun onCreateViewModel(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelUtils.createViewModel<BaseViewModel>(this, viewModelKey(), {
            onCreateViewModel()
        }) as VM
        viewModel = vm
        ViewModelLifecycleDispatcher.attachTo(vm, lifecycle)
    }

    protected open fun viewModelKey(): String = javaClass.canonicalName
}