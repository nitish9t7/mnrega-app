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

package dev.galaxius.mnrega.simpleapp.view.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.galaxius.mnrega.simpleapp.R
import dev.galaxius.mnrega.simpleapp.databinding.NoteDetailFragmentBinding
import dev.galaxius.mnrega.simpleapp.view.base.BaseFragment
import dev.galaxius.mnrega.utils.ext.showDialog
import dev.galaxius.mnrega.utils.ext.toStringOrEmpty
import dev.galaxius.mnrega.utils.saveBitmap
import dev.galaxius.mnrega.utils.share.shareImage
import dev.galaxius.mnrega.utils.share.shareNoteText
import dev.galaxius.mnrega.view.state.NoteDetailState
import dev.galaxius.mnrega.view.viewmodel.NoteDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailFragment :
    BaseFragment<NoteDetailFragmentBinding, NoteDetailState, NoteDetailViewModel>() {

    private val args: NoteDetailFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelAssistedFactory: NoteDetailViewModel.Factory

    /**
     * Since we are continuously listening to the [NoteDetailState] for state updates, we get
     * initial title and attendance from this model. Also, we continuously tell [NoteDetailViewModel]
     * about changes to title and attendance and that ViewModel again let us know about the changes
     * through the new state. So this forms a continuous cycle of events which can then lead
     * to the issues. So using this field, we can make sure that whether attendance is loaded or not.
     * Once the attendance is loaded initially, we won't respect further state changes of title and attendances.
     */
    private var isNoteLoaded = false

    override val viewModel: NoteDetailViewModel by viewModels {
        args.attendanceId?.let { attendanceId ->
            NoteDetailViewModel.provideFactory(viewModelAssistedFactory, attendanceId)
        } ?: throw IllegalStateException("'attendanceId' shouldn't be null")
    }

    private val requestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) shareImage() else showErrorDialog(
            title = getString(R.string.dialog_title_failed_image_share),
            message = getString(R.string.dialog_message_failed_image_share)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initView() {
        binding.run {
            fabSave.setOnClickListener { viewModel.save() }
            attendanceLayout.run {
                fieldTitle.addTextChangedListener { viewModel.setTitle(it.toStringOrEmpty()) }
                fieldNote.addTextChangedListener { viewModel.setNote(it.toStringOrEmpty()) }
            }
        }
    }

    override fun render(state: NoteDetailState) {
        showProgressDialog(state.isLoading)

        binding.fabSave.isVisible = state.showSave

        val title = state.title
        val attendance = state.attendance

        if (title != null && attendance != null && !isNoteLoaded) {
            isNoteLoaded = true
            binding.attendanceLayout.fieldTitle.setText(title)
            binding.attendanceLayout.fieldNote.setText(attendance)
        }

        if (state.finished) {
            findNavController().navigateUp()
        }

        val errorMessage = state.error
        if (errorMessage != null) {
            toast("Error: $errorMessage")
        }
    }

    private fun shareText() {
        val title = binding.attendanceLayout.fieldTitle.text.toString()
        val attendance = binding.attendanceLayout.fieldNote.text.toString()

        requireContext().shareNoteText(title, attendance)
    }

    private fun shareImage() {
        if (!isStoragePermissionGranted()) {
            requestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        val imageUri = binding.attendanceLayout.attendanceContentLayout.drawToBitmap().let { bitmap ->
            saveBitmap(requireActivity(), bitmap)
        } ?: run {
            toast("Error occurred!")
            return
        }

        requireContext().shareImage(imageUri)
    }

    private fun isStoragePermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.attendance_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> confirmNoteDeletion()
            R.id.action_share_text -> shareText()
            R.id.action_share_image -> shareImage()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = NoteDetailFragmentBinding.inflate(inflater, container, false)

    private fun confirmNoteDeletion() {
        showDialog(
            title = "Delete?",
            message = "Sure want to delete the attendance?",
            positiveActionText = "Yes",
            positiveAction = { _, _ ->
                viewModel.delete()
            },
            negativeActionText = "No",
            negativeAction = { dialog, _ ->
                dialog.dismiss()
            }
        )
    }
}
