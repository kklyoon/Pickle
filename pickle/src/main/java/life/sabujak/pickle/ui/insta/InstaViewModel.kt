package life.sabujak.pickle.ui.insta

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import life.sabujak.pickle.ui.dialog.Config
import life.sabujak.pickle.data.cursor.CursorType
import life.sabujak.pickle.data.cursor.ImageCursorFactory
import life.sabujak.pickle.data.datasource.PickleDataSourceFactory
import life.sabujak.pickle.data.entity.Media
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.data.entity.PickleResult
import life.sabujak.pickle.util.Logger
import java.util.ArrayList

class InstaViewModel(application: Application) : AndroidViewModel(application)
//    , PickleItem.Handler
{

    val logger = Logger.getLogger(this.javaClass.simpleName)

    private val _isAspectRatio = MutableLiveData<Boolean>(false)
    var isAspectRatio: LiveData<Boolean> = _isAspectRatio
    private val _isMultipleSelect = MutableLiveData<Boolean>(false)
    var isMultipleSelect: LiveData<Boolean> = _isMultipleSelect
    lateinit var selectedItem: PickleItem
    var config: InstaConfig? = null

    val selectionManager = InstaSelectionManager()

    private val dataSourceFactory by lazy {
        PickleDataSourceFactory(
            application,
            ImageCursorFactory()
        )
    }
    val items: LiveData<PagedList<PickleItem>> =
        LivePagedListBuilder(dataSourceFactory, 50).build()

    init {
        dataSourceFactory.currentDataSource
    }

    fun ratioClicked() {
        if (_isAspectRatio.value == true) {
            _isAspectRatio.postValue(false)
        }
        else {
            _isAspectRatio.postValue(true)
        }
    }

    fun setSelected(selected: PickleItem){
        selectedItem = selected
    }

    fun multipleClicked() {
        selectionManager.clear()
        if(_isMultipleSelect.value == true) {
            _isMultipleSelect.postValue(false)
            selectionManager.setMultipleSelect(false)
        }
        else {
            _isMultipleSelect.postValue(true)
            selectionManager.setMultipleSelect(true)
        }
    }

    fun getPickleResult(): PickleResult {

        val mediaList = ArrayList<Media>()
        for(key in selectionManager.selectionList.keys){
            mediaList.add(key.media)
        }

        return PickleResult(mediaList)
    }
}
