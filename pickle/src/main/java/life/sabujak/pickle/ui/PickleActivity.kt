package life.sabujak.pickle.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import life.sabujak.pickle.R
import life.sabujak.pickle.data.PickleContentObserver
import life.sabujak.pickle.databinding.ActivityPickleBinding
import life.sabujak.pickle.util.Logger

class PickleActivity : AppCompatActivity() {

    private val logger = Logger.getLogger("PickleActivity")
    private val picklePermission = PicklePermission(this)
    private val disposables = CompositeDisposable()
    private val viewModel: PickleViewModel by viewModels()

    private lateinit var binding: ActivityPickleBinding
    private lateinit var contentObserver: PickleContentObserver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d("onCreate")
        disposables.add( picklePermission.request().subscribe { granted ->
            logger.d("pickle permission = $granted")
            if(granted){
                initUI()
            }else{
                logger.w("pickle permission has not granted")
            }
        })
    }

    private fun initUI(){
        logger.d("initUI")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pickle)
        contentObserver = PickleContentObserver(this)
        contentObserver.contentChangedEvent.observe(this,
            Observer {
                logger.i("onChangedEvent From PickleContentObserver")
                viewModel.invalidateDataSource()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!disposables.isDisposed){
            disposables.dispose()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        picklePermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
        logger.d("onRequestPermissionsResult")
    }
}
