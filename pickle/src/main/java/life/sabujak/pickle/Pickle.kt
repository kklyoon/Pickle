package life.sabujak.pickle

import androidx.fragment.app.FragmentManager
import life.sabujak.pickle.data.cursor.CursorType
import life.sabujak.pickle.ui.common.OnResultListener
import life.sabujak.pickle.ui.dialog.Config
import life.sabujak.pickle.ui.dialog.PickleDialogFragment
import life.sabujak.pickle.ui.insta.InstaConfig
import life.sabujak.pickle.ui.insta.InstaFragment

class Pickle {
    companion object{
    @JvmStatic
    fun start(fragmentManager: FragmentManager, listener: OnResultListener) {
        PickleDialogFragment(Config.Builder(listener).build()).show(
            fragmentManager,
            PickleDialogFragment::class.simpleName
        )
    }
    @JvmStatic
    fun startInsta(fragmentManager: FragmentManager, containerId: Int, listener: OnResultListener) {
        fragmentManager.beginTransaction()
            .add(containerId, InstaFragment(InstaConfig.Builder(listener).build()), "Insta")
            .addToBackStack(null)
            .commit()
        }
    }

}