package life.sabujak.pickle

import androidx.fragment.app.FragmentManager
import life.sabujak.pickle.ui.common.OnResultListener
import life.sabujak.pickle.ui.dialog.PickleDialogFragment
import life.sabujak.pickle.ui.insta.InstaFragment

object Pickle {
    fun start(fragmentManager: FragmentManager, listener: OnResultListener) {
        PickleDialogFragment(Config.Builder(listener).build()).show(
            fragmentManager,
            PickleDialogFragment::class.simpleName
        )
    }


    fun startInsta(fragmentManager: FragmentManager, containerId: Int, listener: OnResultListener){
        fragmentManager.beginTransaction()
            .add(containerId, InstaFragment(Config.Builder(listener, type = Config.Type.INSTA).build()),"Insta")
            .addToBackStack(null)
            .commit()
    }
}