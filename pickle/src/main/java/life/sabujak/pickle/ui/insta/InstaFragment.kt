package life.sabujak.pickle.ui.insta

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_insta.*
import kotlinx.android.synthetic.main.fragment_insta.view.*
import life.sabujak.pickle.R
import life.sabujak.pickle.databinding.FragmentInstaBinding
import life.sabujak.pickle.ui.PickleViewModel
import life.sabujak.pickle.util.Logger

class InstaFragment : Fragment() {

    val logger = Logger.getLogger("InstaFragment")

    lateinit var binding: FragmentInstaBinding
    lateinit var viewModel:PickleViewModel
    lateinit var preViewModel:PreViewModel
    val adapter = InstaAdapter()
    val gridLayoutManager by lazy {
        GridLayoutManager(context, 3)
    }

    var selectedPosition: Int = -1

    val flingAnimationY: FlingAnimation by lazy(LazyThreadSafetyMode.NONE){
        FlingAnimation(preview_layout, DynamicAnimation.Y).setFriction(1.1f).setMaxValue(preview_layout.height.toFloat())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as AppCompatActivity).supportActionBar?.hide()
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(PickleViewModel::class.java)
            preViewModel = ViewModelProviders.of(it).get(PreViewModel::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d("onCreate")
        viewModel.items.observe(this, Observer { pagedList ->
            adapter.submitList(pagedList)
        })
        preViewModel.scaleType.observe(this, Observer { scaleType ->
            iv_preview.scaleType = scaleType
            iv_preview.drawable?.let{
                loadImageView(selectedPosition)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insta, container, false)
        binding.viewModel = preViewModel
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = gridLayoutManager
        adapter.itemClick = object: InstaAdapter.ItemClick{
            override fun onClick(view: View, position: Int) {
                selectedPosition = position
                loadImageView(position)
                animateForSelection(view)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun loadImageView(position: Int){
        val selected = adapter.getPickleMeida(position)
        selected?.getUri()?.let{
            Glide.with(iv_preview).load(it).into(iv_preview)
        }
    }
    fun animateForSelection(selectedView: View){
        // fling down animation preview_layout
//        FlingAnimation(preview_layout, DynamicAnimation.SCROLL_Y).apply{
//            setStartVelocity(-100)
//            setMinValue(0)
//            setMaxValue(preview_layout.height.toFloat())
//            start()
//        }
//        flingAnimationY.setStartVelocity()
        flingAnimationY.start()

        // scroll recyclerview to selected view

    }
}