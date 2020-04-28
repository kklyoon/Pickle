package life.sabujak.pickle.ui.insta

import androidx.databinding.BaseObservable
import androidx.lifecycle.MutableLiveData
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.ui.insta.internal.CropData
import life.sabujak.pickle.util.Logger

class InstaSelectionManager : BaseObservable() {
    val logger = Logger.getLogger(this.javaClass.simpleName)
    private var lastSelected: PickleItem? = null
    val selectionList = LinkedHashMap<PickleItem, CropData?>()

    val count = MutableLiveData(0)

    val isMultiSelect = MutableLiveData<Boolean>(false)
    val isCrop = MutableLiveData<Boolean>(false)


    fun setMultipleSelect(isMultiple: Boolean) {
        logger.d("setMultipleSelect ${isMultiple}")
        isMultiSelect.postValue(isMultiple)
        lastSelected?.let {setChecked(it, isMultiple)}
    }

    fun isChecked(pickleItem: PickleItem): Boolean{
        return lastSelected == pickleItem
    }

    private fun isMultiSelect() =  isMultiSelect.value

    private fun isCropSelect() =  isCrop.value


    private fun setChecked(pickleItem: PickleItem, checked: Boolean) {
        if (checked) {
            selectionList[pickleItem] = null
        } else {
            selectionList.remove(pickleItem)
        }

        updateCount()
        notifyChange()
    }

    private fun setChecked(pickleItem: PickleItem, checked: Boolean, cropData: CropData) {
        if (checked) {
            selectionList[pickleItem] = cropData
        } else {
            selectionList.remove(pickleItem)
        }

        updateCount()
        notifyChange()
    }

    fun itemClick(pickleItem: PickleItem, cropData: CropData?) {
        if (isMultiSelect() == true && isCropSelect() == true) {
            cropData?.let {
                setChecked(pickleItem, !isChecked(pickleItem), it)
            }
        } else {
            setChecked(pickleItem, !isChecked(pickleItem))
        }
        if (lastSelected == pickleItem) return
        lastSelected = pickleItem
        notifyChange()
    }

    fun updateCropData(pickleItem: PickleItem, cropData: CropData?){
        cropData?.let{
            selectionList.put(pickleItem, cropData)
        }
        logger.d("$cropData")
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

    fun clear() {
        selectionList.clear()
        updateCount()
        notifyChange()
    }

    fun setAspectRatio(){
        for(key in selectionList.keys){
            selectionList[key] = null
        }
    }

    fun hasCropData(id: PickleItem) = selectionList[id] != null

}
