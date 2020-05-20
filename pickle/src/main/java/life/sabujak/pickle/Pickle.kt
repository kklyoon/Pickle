package life.sabujak.pickle

import androidx.fragment.app.FragmentManager
import life.sabujak.pickle.ui.dialog.Config
import life.sabujak.pickle.ui.dialog.OnResultListener
import life.sabujak.pickle.ui.dialog.PickleDialogFragment
import life.sabujak.pickle.ui.insta.InstaConfig
import life.sabujak.pickle.ui.insta.InstaFragment
import life.sabujak.pickle.ui.insta.internal.OnInstaResultListener

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
    fun startInsta(fragmentManager: FragmentManager, listener: OnInstaResultListener) {
        fragmentManager.beginTransaction()
            .add(android.R.id.content, InstaFragment(InstaConfig.Builder(listener).build()), "Insta")
            .addToBackStack(null)
            .commit()
        }
    }

}