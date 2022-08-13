/*
 * Copyright 2022 Team GALAXIUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.galaxius.mnrega.simpleapp.view.attendances

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.galaxius.mnrega.core.model.Note
import dev.galaxius.mnrega.simpleapp.R
import dev.galaxius.mnrega.simpleapp.databinding.NotesFragmentBinding
import dev.galaxius.mnrega.simpleapp.view.base.BaseFragment
import dev.galaxius.mnrega.simpleapp.view.hiltMnregaMainNavGraphViewModels
import dev.galaxius.mnrega.simpleapp.view.attendances.adapter.NotesListAdapter
import dev.galaxius.mnrega.utils.autoCleaned
import dev.galaxius.mnrega.utils.ext.hide
import dev.galaxius.mnrega.utils.ext.setDrawableLeft
import dev.galaxius.mnrega.utils.ext.show
import dev.galaxius.mnrega.utils.ext.showDialog
import dev.galaxius.mnrega.view.state.NotesState
import dev.galaxius.mnrega.view.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : BaseFragment<NotesFragmentBinding, NotesState, NotesViewModel>() {

    override val viewModel: NotesViewModel by hiltMnregaMainNavGraphViewModels()

    private val attendancesListAdapter by autoCleaned(initializer = { NotesListAdapter(::onNoteClicked) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initView() {
        binding.run {
            attendancesRecyclerView.adapter = attendancesListAdapter
            fabNew.setOnClickListener {
                findNavController().navigate(R.id.action_attendancesFragment_to_addNoteFragment)
            }
            swipeRefreshNotes.apply {
                setColorSchemeColors(
                    ContextCompat.getColor(requireContext(), R.color.secondaryColor),
                    ContextCompat.getColor(requireContext(), R.color.onSecondary)
                )
                setOnRefreshListener { viewModel.syncNotes() }
            }
        }
    }

    override fun render(state: NotesState) {
        binding.swipeRefreshNotes.isRefreshing = state.isLoading
        binding.swipeRefreshNotes.isEnabled = state.isConnectivityAvailable == true

        val errorMessage = state.error
        if (errorMessage != null) {
            toast("Error: $errorMessage")
        }

        val attendances = state.attendances
        if (attendances.isNotEmpty()) {
            onNotesLoaded(attendances)
        }

        if (state.isUserLoggedIn == false) {
            logout()
        }

        val isConnectivityAvailable = state.isConnectivityAvailable
        if (isConnectivityAvailable != null) {
            if (isConnectivityAvailable) {
                onConnectivityAvailable()
            } else {
                onConnectivityUnavailable()
            }
        }
    }

    private fun onNotesLoaded(data: List<Note>) {
        binding.emptyStateLayout.run {
            if (data.isEmpty()) show() else hide()
        }
        attendancesListAdapter.submitList(data)
    }

    private fun onNoteClicked(attendance: Note) {
        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToNoteDetailFragment(attendance.id)
        )
    }

    private fun onConnectivityUnavailable() {
        with(binding) {
            textNetworkStatus.apply {
                setDrawableLeft(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_connectivity_unavailable
                    )
                )
                text = getString(R.string.text_no_connectivity)
            }

            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(resources, R.color.error, requireActivity().theme)
                )
            }.also { it.show() }
        }
    }

    private fun onConnectivityAvailable() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            if (shouldSyncNotes()) { viewModel.syncNotes() }
        }
        with(binding) {
            textNetworkStatus.apply {
                setDrawableLeft(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_connectivity_available
                    )
                )
                text = getString(R.string.text_connectivity)
            }

            networkStatusLayout.apply {
                setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.success,
                        requireActivity().theme
                    )
                )
            }.also {
                it.animate()
                    .alpha(1f)
                    .setStartDelay(ANIMATION_DURATION)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            it.hide()
                        }
                    })
            }
        }
    }

    private fun isConnected(): Boolean? = viewModel.currentState.isConnectivityAvailable

    private fun shouldSyncNotes() = viewModel.currentState
        .let { state -> state.error != null || state.attendances.isEmpty() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NotesFragmentBinding.inflate(inflater, container, false)

    override fun onPrepareOptionsMenu(menu: Menu) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (viewModel.isDarkModeEnabled()) {
                true -> {
                    menu.findItem(R.id.action_dark_mode).isVisible = false
                }
                false -> {
                    menu.findItem(R.id.action_light_mode).isVisible = false
                }
            }
            super.onPrepareOptionsMenu(menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_light_mode -> viewModel.setDarkMode(false)
            R.id.action_dark_mode -> viewModel.setDarkMode(true)
            R.id.action_about ->
                findNavController().navigate(R.id.action_attendancesFragment_to_aboutFragment)
            R.id.action_logout -> confirmLogout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmLogout() {
        showDialog(
            title = "Logout?",
            message = "Sure want to logout?",
            positiveActionText = "Yes",
            positiveAction = { _, _ -> viewModel.logout() },
            negativeActionText = "No",
            negativeAction = { dialog, _ ->
                dialog.dismiss()
            }
        )
    }

    private fun logout() {
        val destination = NotesFragmentDirections.actionNotesFragmentToLoginFragment()
        if (isAdded) {
            with(findNavController()) {
                currentDestination?.getAction(destination.actionId)?.let {
                    navigate(destination)
                }
            }
        } else return
    }

    companion object {
        const val ANIMATION_DURATION = 2000L
    }
}
