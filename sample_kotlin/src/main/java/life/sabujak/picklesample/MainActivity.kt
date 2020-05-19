package life.sabujak.picklesample

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import life.sabujak.pickle.Pickle
import life.sabujak.pickle.data.entity.PickleError
import life.sabujak.pickle.ui.dialog.OnResultListener
import life.sabujak.pickle.ui.dialog.PickleResult
import life.sabujak.pickle.ui.insta.internal.InstaResult
import life.sabujak.pickle.ui.insta.internal.OnInstaResultListener
import life.sabujak.picklesample.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.basic.setOnClickListener {
            Pickle.start(supportFragmentManager, object :
                OnResultListener {
                override fun onSuccess(result: PickleResult) {
                    result.mediaList.forEach {
                        Log.e("MainActivity", it.toString())
                    }
                    val adapter = RvAdapter(ArrayList(result.mediaList))
                    binding.rlView.adapter = adapter
                }

                override fun onError(pickleError: PickleError) {
                    TODO("Not yet implemented")
                }
            })
        }

        binding.insta.setOnClickListener {
            Pickle.startInsta(supportFragmentManager, R.id.frag_container, object :
                OnInstaResultListener {
                override fun onSuccess(result: InstaResult) {
                    result.mediaList.forEach {
                        Log.e("MainActivity", it.toString())
                    }
                    val adapter = RvAdapter(ArrayList(result.mediaList))
                    binding.rlView.adapter = adapter
                }

                override fun onError(pickleError: PickleError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        actionBar?.show()
    }
}
