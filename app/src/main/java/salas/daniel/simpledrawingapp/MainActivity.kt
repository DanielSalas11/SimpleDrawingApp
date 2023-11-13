package salas.daniel.simpledrawingapp


import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.get
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val cameraResultLauncher : ActivityResultLauncher<String> =
            registerForActivityResult(ActivityResultContracts.RequestPermission()){
                    isGranted ->
                if(isGranted){
                    Toast.makeText(this,"Permission was granted",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this,"Permission was denied",Toast.LENGTH_SHORT).show()
                    }
            }
    private val requestPermissions : ActivityResultLauncher<Array<String>> =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
                permissions.entries.forEach{
                    val permissionName = it.key
                    val isGranted = it.value
                    if(isGranted){
                        Toast.makeText(this@MainActivity,"Storage permission was granted",Toast.LENGTH_SHORT).show()

                        val pickIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        openGalleryLauncher.launch(pickIntent)

                    }else{
                        if(permissionName==Manifest.permission.READ_EXTERNAL_STORAGE){
                            Toast.makeText(this@MainActivity,"Storage permission was denied",Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data!=null){
                val imageBackGround:ImageView = findViewById(R.id.iv_background)

                imageBackGround.setImageURI(result.data?.data)
            }
        }

    private val cameraAndLocationResultLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted){
                    if(permissionName == Manifest.permission.ACCESS_FINE_LOCATION){
                     Toast.makeText(this,"Permission is granted for fine location", Toast.LENGTH_SHORT).show()
                    }
                    if(permissionName == Manifest.permission.ACCESS_COARSE_LOCATION){
                        Toast.makeText(this,"Permission was granted for coarse location", Toast.LENGTH_SHORT).show()
                    }
                    if(permissionName == Manifest.permission.CAMERA){
                        Toast.makeText(this,"Permission is granted for camera", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(permissionName == Manifest.permission.ACCESS_FINE_LOCATION){
                        Toast.makeText(this,"Permission was denied for fine location", Toast.LENGTH_SHORT).show()
                    }
                    if(permissionName == Manifest.permission.ACCESS_COARSE_LOCATION){
                        Toast.makeText(this,"Permission was denied for coarse location", Toast.LENGTH_SHORT).show()
                    }
                    if(permissionName == Manifest.permission.CAMERA){
                        Toast.makeText(this,"Permission was denied for camera", Toast.LENGTH_SHORT).show()
                }

            }
            }
        }

    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)

        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )

        val ib_brush: ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        val ib_undo: ImageButton = findViewById(R.id.ib_undo)
        ib_undo.setOnClickListener {
            drawingView?.onClickUndo()
        }
        val ib_redo: ImageButton = findViewById(R.id.ib_redo)
        ib_redo.setOnClickListener {
            drawingView?.onClickRedo()
        }
        val ib_save: ImageButton = findViewById(R.id.ib_save)
        ib_save.setOnClickListener {
            drawingView?.onClickSave()
        }

        val ib_gallery: ImageButton = findViewById(R.id.ib_gallery)
        /*ib_gallery.setOnClickListener {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            showRationaleDialog("Permission Demo requires camera access",
            "Camera cannot be used because Camera access was denied")
            }else{
                cameraResultLauncher.launch(Manifest.permission.CAMERA)
            }
        }*/
        /*ib_gallery.setOnClickListener {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                showRationaleDialog("Permission Demo requires camera access",
                    "Camera cannot be used because Camera access was denied")
            }else{
                /*cameraAndLocationResultLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))*/
                readExternalStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }*/

        ib_gallery.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            showRationaleDialog(
                "Kids Drawing App", "Kids Drawing App needs to Access your External Storage")
        } else {
            requestPermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            //TODO - Add writing external storage permision
        }
    }
    private fun showRationaleDialog(title:String, message: String,) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showBrushSizeChooserDialog(){
        var brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialogue_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn:ImageButton= brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn:ImageButton= brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn:ImageButton= brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }
    fun paintClicked(view: View){
        if(view != mImageButtonCurrentPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_selected)
            )

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            mImageButtonCurrentPaint = view

        }
    }
}