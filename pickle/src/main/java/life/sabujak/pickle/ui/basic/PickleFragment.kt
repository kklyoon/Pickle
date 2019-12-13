package life.sabujak.pickle.ui.basic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import life.sabujak.pickle.R
import life.sabujak.pickle.data.entity.PickleMedia
import life.sabujak.pickle.databinding.FragmentPickleBinding
import life.sabujak.pickle.ui.common.OnEventListener
import life.sabujak.pickle.ui.common.OptionMenuViewModel
import life.sabujak.pickle.ui.common.PickleViewModel
import life.sabujak.pickle.util.Calculator
import life.sabujak.pickle.util.Logger
import life.sabujak.pickle.util.ext.showToast


class PickleFragment : Fragment(),OnEventListener {

    val logger = Logger.getLogger(PickleFragment::class.java.simpleName)

    lateinit var binding: FragmentPickleBinding
    lateinit var viewModel: PickleViewModel
    private val adapter by lazy { PickleAdapter(lifecycle, viewModel.selectionManager, this) }
    private val gridLayoutManager by lazy {
        GridLayoutManager(
            context,
            Calculator.getColumnCount(context, R.dimen.pickle_column_width)
        )
    }
    lateinit var optionMenuViewModel :OptionMenuViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(PickleViewModel::class.java)
            optionMenuViewModel = ViewModelProviders.of(it).get(OptionMenuViewModel::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        logger.d("onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pickle, container, false)
        binding.recyclerView.adapter = adapter
        binding.lifecycleOwner = viewLifecycleOwner
        binding.recyclerView.layoutManager = gridLayoutManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.items.observe(viewLifecycleOwner, Observer { pagedList ->
            adapter.submitList(pagedList)
        })
        optionMenuViewModel.clickEvent.observe(viewLifecycleOwner, Observer {
            showToast("선택된 아이템은 로그에서 확인")
            viewModel.selectionManager.selectionList.forEach { value ->
                logger.i("$value")
            }
        })

        viewModel.selectionManager.count.observe(viewLifecycleOwner, Observer {
            optionMenuViewModel.count.value = it
        })

        viewModel.initialLoadState.observe(viewLifecycleOwner, Observer {
            logger.d("initialLoadState = $it")
        } )
        viewModel.dataSourceState.observe(viewLifecycleOwner, Observer {
            logger.d("dataSourceState = $it")
        })
    }

    override fun onItemClick(pickleMedia: PickleMedia) {
        showToast("${pickleMedia.getId()}")
    }

    override fun onItemLongClick(pickleMedia: PickleMedia): Boolean {
        showToast("${pickleMedia.getId()}")
        return false
    }

}
