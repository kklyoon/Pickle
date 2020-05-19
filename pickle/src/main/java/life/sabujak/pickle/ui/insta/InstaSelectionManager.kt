package life.sabujak.pickle.ui.insta

import androidx.databinding.BaseObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.ui.insta.internal.CropData
import life.sabujak.pickle.util.Logger

class InstaSelectionManager : BaseObservable() {
    val logger = Logger.getLogger(this.javaClass.simpleName)
    private var lastSelected: PickleItem? = null
    val selectionList = LinkedHashMap<PickleItem, CropData?>()

    val count = MutableLiveData(0)

    private val _isMultiSelect = MutableLiveData<Boolean>(false)
    val isMultiSelect: LiveData<Boolean> = _isMultiSelect

    fun setMultipleSelect(isMultiple: Boolean) {
        logger.d("setMultipleSelect ${isMultiple}")
        _isMultiSelect.postValue(isMultiple)
        updateCount()
        notifyChange()
    }

    fun isChecked(pickleItem: PickleItem): Boolean {
        if(isMultiSelect.value == true)
            return selectionList.containsKey(pickleItem)
        else
            return lastSelected == pickleItem
    }

    fun isClicked(pickleItem: PickleItem) = lastSelected == pickleItem

    fun itemClick(pickleItem: PickleItem){
        if(isMultiSelect.value == false) {
            selectionList.remove(lastSelected)
        }
        if(!selectionList.containsKey(pickleItem))
            selectionList[pickleItem] = null

        lastSelected = pickleItem
        updateCount()
        notifyChange()
    }

    fun updateCropData(pickleItem: PickleItem, cropData: CropData?) {
        cropData?.let {
            selectionList.put(pickleItem, cropData)
        }
        logger.d("ItemID: ${pickleItem.getId()} listsize :${selectionList.size}, $cropData")
    }

    private fun updateCount() {
        this.count.value = selectionList.size
    }

    private fun getIndex(id: PickleItem) = ArrayList(selectionList.keys).indexOf(id)


    fun getPosition(pickleItem: PickleItem): String {
        val index = getIndex(pickleItem)
        return if (index < 0) {
            ""
        } else {
            "${index + 1}"
        }
    }

    fun setMultiCropData(pickleItem: PickleItem? = lastSelected, cropData: CropData) {
        pickleItem?.let {
            selectionList[it] = cropData
            logger.d("setMultiCropData : ${it}, $cropData")
        }
    }

    fun toggleIndex(pickleItem: PickleItem){
        if(selectionList.containsKey(pickleItem)) {
            selectionList.remove(pickleItem)
            updateCount()
            notifyChange()
        }
    }

    fun clear() {
        selectionList.clear()
        updateCount()
        notifyChange()
    }

    fun clearCropData(){
        for(key in selectionList.keys){
            selectionList[key] = null
        }
    }

    fun getCropData(id: PickleItem) = selectionList[id]

    fun isLast(id:PickleItem) = lastSelected == id

}
