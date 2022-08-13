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

package dev.galaxius.mnrega.simpleapp.view.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.galaxius.mnrega.simpleapp.databinding.AddNoteFragmentBinding
import dev.galaxius.mnrega.simpleapp.view.base.BaseFragment
import dev.galaxius.mnrega.simpleapp.view.hiltMnregaMainNavGraphViewModels
import dev.galaxius.mnrega.utils.ext.toStringOrEmpty
import dev.galaxius.mnrega.view.state.AddNoteState
import dev.galaxius.mnrega.view.viewmodel.AddNoteViewModel

@AndroidEntryPoint
class AddNoteFragment : BaseFragment<AddNoteFragmentBinding, AddNoteState, AddNoteViewModel>() {

    override val viewModel: AddNoteViewModel by hiltMnregaMainNavGraphViewModels()

    override fun initView() {
        binding.run {
            fabSave.setOnClickListener { viewModel.add() }
            attendanceLayout.run {
                fieldTitle.addTextChangedListener { viewModel.setTitle(it.toStringOrEmpty()) }
                fieldNote.addTextChangedListener { viewModel.setNote(it.toStringOrEmpty()) }
            }
        }
    }

    override fun render(state: AddNoteState) {
        binding.fabSave.isVisible = state.showSave

        showProgressDialog(state.isAdding)

        if (state.added) {
            findNavController().navigateUp()
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            showErrorDialog("Failed to add a attendance", errorMessage)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = AddNoteFragmentBinding.inflate(inflater, container, false)

    override fun onDestroyView() {
        viewModel.resetState()
        super.onDestroyView()
    }
}
