package life.sabujak.pickle.ui.insta.internal

import life.sabujak.pickle.data.entity.PickleError

interface OnInstaResultListener{
    fun onSuccess(result: InstaResult)
    fun onError(pickleError: PickleError)

}