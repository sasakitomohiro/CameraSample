package com.github.sasakitomohiro.camerasample

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import com.github.sasakitomohiro.camerasample.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream

class CameraActivity : AppCompatActivity() {
    companion object {
        fun createIntent(context: Context) = Intent(context, CameraActivity::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityCameraBinding>(this, R.layout.activity_camera)
    }

    private val cameraLoader by lazy {
        Camera2Loader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraLoader.setOnPreviewFrameListener { data, width, height ->
            val bitmap = data.nv21ToBitmap(width, height)
            val rotateBitmap = bitmap.rotateImage(cameraLoader.getCameraOrientation())
            val canvas = binding.surfaceView.holder.lockCanvas()
            canvas.drawBitmap(
                rotateBitmap,
                binding.surfaceView.left.toFloat(),
                binding.surfaceView.top.toFloat(),
                Paint()
            )
            binding.surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.surfaceView.doOnLayout {
            cameraLoader.onResume(it.width, it.height)
        }
    }

    override fun onPause() {
        super.onPause()

        cameraLoader.onPause()
    }
}

private fun ByteArray.nv21ToBitmap(width: Int, height: Int): Bitmap {
    val yuvImage = YuvImage(this, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun Bitmap.rotateImage(orientation: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(orientation.toFloat())
    }
    val rotateBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return rotateBitmap
}
