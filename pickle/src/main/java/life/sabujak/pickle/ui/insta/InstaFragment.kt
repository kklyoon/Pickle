package life.sabujak.pickle.ui.insta

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import life.sabujak.pickle.Config
import life.sabujak.pickle.R
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.databinding.FragmentInstaBinding
import life.sabujak.pickle.util.Logger
import life.sabujak.pickle.util.ext.showToast

class InstaFragment constructor() : Fragment(), OnInstaEventListener {
    val logger = Logger.getLogger(this.javaClass.simpleName)

    lateinit var binding: FragmentInstaBinding
    private val instaViewModel: InstaViewModel by viewModels()
    private val instaTopViewModel: InstaTopViewModel by viewModels()
    private val instaAdapter by lazy {
        InstaAdapter(lifecycle, instaViewModel.selectionManager, this)
    }
    private val gridLayoutManager by lazy {
        GridLayoutManager(context, 3)
    }
    private lateinit var config: Config

    private val parentJob = Job()
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + parentJob)

    constructor(config: Config) : this() {
        this.config = config
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            (it as? AppCompatActivity)?.supportActionBar?.hide()
            instaTopViewModel.setCountable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (savedInstanceState == null) {
            instaViewModel.config = this.config
        } else {
            this.config = instaViewModel.config!!
        }
        logger.d("onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        logger.d("onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insta, container, false)
        binding.apply {
            viewModel = instaViewModel
            menuViewModel = instaTopViewModel
            recyclerView.adapter = instaAdapter
            lifecycleOwner = viewLifecycleOwner
            recyclerView.layoutManager = gridLayoutManager
            val appBar = previewAppbarLayout
            appBar.layoutParams?.let {
                val behavior = AppBarLayout.Behavior()
                behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                    override fun canDrag(p0: AppBarLayout): Boolean {
                        return false
                    }
                })
                (it as CoordinatorLayout.LayoutParams).behavior = behavior
            }
            ivPreview.setSelectionManager(instaViewModel.selectionManager)
            ivPreview.addOnCropListener(object : OnCropListener {
                override fun onSuccess(bitmap: Bitmap) {
                    logger.d("Bitmap ID : ${bitmap.generationId}")
                    val dialogLayout =
                        layoutInflater.inflate(R.layout.dialog_result, container, false)
                    var dialogImageView = dialogLayout.findViewById<ImageView>(R.id.iv_image)
                    progressbar.visibility = View.GONE
                    context?.let {
                        Glide.with(dialogLayout.context).load(bitmap).fitCenter()
                            .into(dialogImageView)
                        val alertDialog = AlertDialog.Builder(it)
                        alertDialog.setOnDismissListener {
                            logger.d("release bitmap")
                            dialogImageView.setImageDrawable(null)
                            dialogImageView = null
                        }
                        alertDialog.setView(dialogLayout).show()
                    }
                }

                override fun onFailure(e: Exception) {
                    logger.e("Failed to crop image. msg : ${e.message}")
                }
            })
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        instaViewModel.items.observe(viewLifecycleOwner, Observer { pagedList ->
            logger.d("submitList to Adapter")
            instaAdapter.submitList(pagedList, Runnable {
                pagedList?.let {
                    if (it.size != 0) onItemClick(null, it[0]!!)
                }
            })
        })
        instaViewModel.isAspectRatio.observe(viewLifecycleOwner, Observer {
            if (!binding.ivPreview.isEmpty()) {
                if (it) binding.ivPreview.setAspectRatio() else binding.ivPreview.setCropScale()
            }
        })
        instaViewModel.isMultipleSelect.observe(viewLifecycleOwner, Observer { it ->
            if (it && !binding.ivPreview.isEmpty()) {
                binding.ivPreview.getCropData()?.let { data ->
                    instaViewModel.selectionManager.setMultiCropData(cropData = data)
                }
            }
            // #99 delete later
            if (it) binding.ivRatio.visibility = INVISIBLE
            else binding.ivRatio.visibility = VISIBLE
            instaViewModel.setAspectRatio(it)

            instaTopViewModel.setCountable(it)
        })

        instaTopViewModel.clickEvent.observe(viewLifecycleOwner, Observer {
            activity?.let {
                if (instaViewModel.selectedItem.media.mediaType != MEDIA_TYPE_IMAGE) {
                    showToast("video is not supported now")
                } else {
                    if (instaViewModel.isAspectRatio.value == false) {
                        coroutineScope.launch(Dispatchers.Main) {
                            binding.progressbar.visibility = VISIBLE
                            binding.ivPreview.crop()
                        }
                    } else {
                        config.onResultListener.onSuccess(instaViewModel.getPickleResult())
                        it.supportFragmentManager.popBackStack()
                    }
                }
            }
        })
        instaTopViewModel.isCountVisible.observe(viewLifecycleOwner, Observer {
            if (it) {
                instaViewModel.selectionManager.count.observe(
                    viewLifecycleOwner,
                    Observer { countInfo ->
                        instaTopViewModel.count.postValue(countInfo)
                    })
            } else {
                instaViewModel.selectionManager.count.removeObservers(viewLifecycleOwner)
            }
        })
    }

    private fun loadPickleMedia(item: PickleItem) {
        if (item.media.mediaType == MEDIA_TYPE_IMAGE) {
            binding.ivPreview.setPickleMedia(item)
            if (!binding.ivPreview.isEmpty()) {
                if (instaViewModel.isAspectRatio.value == true) binding.ivPreview.setAspectRatio() else binding.ivPreview.setCropScale()
            }
        }
//        TODO("VIDEO 에 대한 처리")
    }

    override fun onItemClick(view: View?, item: PickleItem) {
        instaViewModel.setSelected(item)
        instaViewModel.selectionManager.itemClick(
            item,
            binding.ivPreview.getCropData()
        )
        if (instaViewModel.selectedItem.media.mediaType != MEDIA_TYPE_IMAGE) {
            showToast("video is not supported now")
            binding.ivPreview.clear()
            return
        }
        loadPickleMedia(item)
        view?.let {
            binding.recyclerView.smoothScrollBy(0, it.top)
            binding.previewAppbarLayout.setExpanded(true)
        }
    }

    private fun showActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDetach() {
        showActionBar()
        super.onDetach()
    }
}
