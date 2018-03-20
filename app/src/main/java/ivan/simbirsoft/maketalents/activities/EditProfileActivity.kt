package ivan.simbirsoft.maketalents.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.PopupMenu
import com.jakewharton.rxbinding2.widget.RxTextView
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.fragments.SimpleMessageDialog
import ivan.simbirsoft.maketalents.rx.SafeFragmentTransactionOperator
import ivan.simbirsoft.maketalents.utils.hideGone
import ivan.simbirsoft.maketalents.utils.setText
import ivan.simbirsoft.maketalents.viewmodel.ViewModelContentState
import ivan.simbirsoft.maketalents.viewmodels.EditProfileViewModel
import ivan.simbirsoft.maketalents.views.CompositeView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.layout_edit_profile.*
import kotlinx.android.synthetic.main.layout_progress_bar_with_darkened_background.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by Ivan Kuznetsov
 * on 20.03.2018.
 */
class EditProfileActivity : ViewModelActivity<EditProfileViewModel>() {

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1
        private const val TAKE_PHOTO_REQUEST = 2
        private const val CHOICE_PHOTO_FROM_GALLERY_REQUEST = 3
    }

    override fun onCreateViewModel() = EditProfileViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        changePhotoTextView.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add(R.string.gallery).setOnMenuItemClickListener {
                startChoicePhotoFromGalleryIntent()
                return@setOnMenuItemClickListener true
            }

            popup.menu.add(R.string.camera).setOnMenuItemClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 5)
                } else {
                    startTakeCaptureIntent()
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }

        val compositeView = view_animator.getChildAt(ViewModelContentState.ERROR_PAGE.index) as CompositeView

        compositeView.setOnButtonClickListener {
            viewModel.repeatButtonClicked()
        }

        viewModel.baseOutputs().currentContentState().compose(bindToLifecycle()).subscribe { contentState ->
            view_animator.displayedChild = contentState.index
        }

        viewModel.baseOutputs().contentErrorText().compose(bindToLifecycle()).subscribe {
            compositeView.setMessageText(it)
        }

        viewModel.baseOutputs().data().compose(bindToLifecycle()).subscribe {
            nameTextInputLayout.setText(it.name)
            nameTextView.text = it.name
            phoneNumberTextInputLayout.setText(it.phoneNumber)
            emailTextInputLayout.setText(it.email)

            updateAvatars(it.avatarUrl)

            RxTextView.textChanges(nameTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.nameWasChanged(it.toString())
                    }

            RxTextView.textChanges(phoneNumberTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.phoneNumberWasChanged(it.toString())
                    }

            RxTextView.textChanges(emailTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.emailWasChanged(it.toString())
                    }
        }

        viewModel.outputs.updateAvatar().compose(bindToLifecycle()).subscribe {
            //todo maybe error
            updateAvatars(it.toString())
        }

        viewModel.outputs.showMessage().compose(bindToLifecycle()).lift(SafeFragmentTransactionOperator(lifecycle)).subscribe {
            SimpleMessageDialog.createInstance(message = it)
                    .show(supportFragmentManager, SimpleMessageDialog.TAG)
        }

        viewModel.outputs.savingProgressBarVisibilityState().compose(bindToLifecycle()).subscribe { hiden ->
            progress_bar_with_darkened_background.hideGone(!hiden)
        }

        viewModel.outputs.finishActivity().compose(bindToLifecycle()).subscribe {
            finish()
        }

        viewModel.outputs.finishActivityWithResult().compose(bindToLifecycle()).subscribe {
            setResult(Activity.RESULT_OK)
            finish()
        }

        saveButton.setOnClickListener {
            viewModel.inputs.saveButtonClicked()
        }
    }

    private fun updateAvatars(url: String) {
        if (url.isEmpty()) {
            Picasso.get().load(ProfileActivity.DEFAULT_AVATAR_URL).into(avatarImageView)
            Picasso.get().load(ProfileActivity.DEFAULT_AVATAR_URL).into(blurredAvatarImageView)
        } else {
            //todo need refactoring
            Picasso.get().load(url).into(avatarImageView)
            Picasso.get().load(url).into(blurredAvatarImageView)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.inputs.actionBarBackButtonClicked()
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOICE_PHOTO_FROM_GALLERY_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            startCropImageIntent(data.data)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                viewModel.inputs.avatarWasSelected(resultUri)
            }
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val photo = data.extras.get("data") as Bitmap
            val bytes = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val file = File(cacheDir, System.currentTimeMillis().toString() + ".jpg")

            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                bytes.writeTo(fileOutputStream)
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }

            val uri = Uri.fromFile(file)

            startCropImageIntent(uri)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            startTakeCaptureIntent()
        }
    }

    private fun startChoicePhotoFromGalleryIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
        startActivityForResult(intent, CHOICE_PHOTO_FROM_GALLERY_REQUEST)
    }

    private fun startTakeCaptureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
        }
    }

    private fun startCropImageIntent(imageUri: Uri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setScaleType(CropImageView.ScaleType.CENTER_INSIDE)
                .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                .start(this)
    }
}