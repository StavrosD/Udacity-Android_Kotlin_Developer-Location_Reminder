package com.udacity.project4.locationreminders.reminderslist

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            //NavController(requireContext()).navigate(ReminderListFragmentDirections.toSaveReminder())
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.setValue(
            NavigationCommand.To(ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter( { reminder ->
            startActivity(
                ReminderDescriptionActivity.newIntent(requireContext(),reminder)
            )
        }, {reminder ->

            AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.verify_delete))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                   // val index = _viewModel.remindersList.value!!.indexOf(reminder)
                    runBlocking {
                        _viewModel.deleteReminder(reminder)
                    }

                //    Log.d("ReminderListFragment", "Reminders count: " + _viewModel.remindersList.value?.size.toString() )
                //      binding.remindersRecyclerView.adapter!!.notifyItemRemoved(index)
                //     binding.remindersRecyclerView.adapter!!.notifyItemRangeChanged(index,1)
                //    binding.remindersRecyclerView.adapter!!.notifyDataSetChanged()

                    // NOT FOR PRODUCTION
                    // The three lines commented above do not update the recyclerview as they should do.
                    // Reloading the reminders is not efficient but a lot of refractoring is needed to update the application.
                    // Removing a reminder is not a project requirement so it is OK to leave it like that. I implemented the delete functionality for testing purposes.
                    _viewModel.loadReminders()

                }
                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        })
//        setup the recycler view using the extension function
            binding.remindersRecyclerView.setup(adapter)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                //                COMPLETED: add the logout implementation
                AuthUI.getInstance().signOut(requireContext())
               activity?.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
}