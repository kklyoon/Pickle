package life.sabujak.pickle.ui.insta

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.view.*
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
import life.sabujak.pickle.R
import life.sabujak.pickle.data.entity.PickleItem
import life.sabujak.pickle.databinding.FragmentInstaBinding
import life.sabujak.pickle.ui.common.OnBitmapResultListener
import life.sabujak.pickle.util.Logger

class InstaFragment constructor() : Fragment(){
    val logger = Logger.getLogger(this.javaClass.simpleName)

    lateinit var binding: FragmentInstaBinding
    private val instaViewModel: InstaViewModel by viewModels()
    private val instaTopViewModel: InstaTopViewModel by viewModels()
    private val instaAdapter by lazy {
        InstaAdapter(lifecycle, instaViewModel.selectionManager, instaViewModel)
    }
    private val gridLayoutManager by lazy {
        GridLayoutManager(context, 3)
    }
    private lateinit var config: InstaConfig

    private val parentJob = Job()
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + parentJob)
    private lateinit var bitmapResultListener: OnBitmapResultListener

    constructor(config: InstaConfig) : this() {
        this.config = config
    }

    constructor(bitmapResultListener: OnBitmapResultListener): this(){
        this.bitmapResultListener = bitmapResultListener
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
            this.config = instaViewModel.config!!           // #99 crash error
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
            ivPreview.setViewModel(instaViewModel)
            ivPreview.addOnCropListener(object : OnCropListener {
                override fun onSuccess(bitmap: Bitmap) {
                    logger.d("Bitmap size : ${bitmap.width} , ${bitmap.height}")
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
                            bitmap.recycle()
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
            instaAdapter.submitList(pagedList) {
                pagedList?.let {
                    if (it.size != 0) instaViewModel.itemClicked(0, it[0]!!)
//                    if (it.size != 0) onItemClick(null, it[0]!!)
                }
            }
        })
        instaViewModel.isAspectRatio.observe(viewLifecycleOwner, Observer {
            if (!binding.ivPreview.isEmpty()) {
                if (it) {
                    binding.ivPreview.setAspectRatio()
                } else binding.ivPreview.setCropScale()
            }
        })
        instaViewModel.isMultipleSelect.observe(viewLifecycleOwner, Observer { it ->
            if (it && instaViewModel.isAspectRatio.value == false) {
                val cropData  = binding.ivPreview.getCropData()
                instaViewModel.selectionManager.setMultiCropData(cropData = cropData)
            }
            instaTopViewModel.setCountable(it)
            binding.recyclerView.adapter?.notifyDataSetChanged()
        })
        instaViewModel.selectedItem.observe(viewLifecycleOwner, Observer {
            loadPickleMedia(it)
            instaViewModel.selectedPosition?.let{position ->
                logger.d("scrollbyposition $position")
                binding.recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.let{itemView->
                    binding.recyclerView.smoothScrollBy(0, itemView.top)
                    binding.previewAppbarLayout.setExpanded(true)}
                }
        })

        instaTopViewModel.clickEvent.observe(viewLifecycleOwner, Observer {
            activity?.let {
                if (instaViewModel.isAspectRatio.value == false) {
                    coroutineScope.launch(Dispatchers.Main) {
                        binding.progressbar.visibility = VISIBLE
                        binding.ivPreview.crop()
                    }
                } else {
                    config.onResultListener?.onSuccess(instaViewModel.getPickleResult())
                    it.supportFragmentManager.popBackStack()
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
            binding.ivPreview.setPickleMedia(item, instaViewModel.isAspectRatio.value)
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
