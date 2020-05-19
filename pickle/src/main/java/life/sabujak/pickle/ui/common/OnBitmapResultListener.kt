package life.sabujak.pickle.ui.common

import life.sabujak.pickle.data.entity.BitmapResult

interface OnBitmapResultListener {
    fun onSuccess(result: BitmapResult)
    fun onFailure(t: Throwable)
}