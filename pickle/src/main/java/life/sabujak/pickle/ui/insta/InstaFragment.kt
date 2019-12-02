package life.sabujak.pickle.ui.insta

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
import life.sabujak.pickle.databinding.FragmentInstaBinding
import life.sabujak.pickle.ui.PickleViewModel
import life.sabujak.pickle.util.Logger

class InstaFragment : Fragment() {

    val logger = Logger.getLogger("InstaFragment")

    lateinit var binding: FragmentInstaBinding
    lateinit var viewModel:PickleViewModel
    val adapter = InstaAdapter()
    val gridLayoutManager by lazy {
        GridLayoutManager(context, 3)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(PickleViewModel::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d("onCreate")
        viewModel.items.observe(this, Observer { pagedList ->
            adapter.submitList(pagedList)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_insta, container, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = gridLayoutManager
        return binding.root
    }

}