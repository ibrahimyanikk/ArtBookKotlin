package com.ibrahim.artbookkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.ibrahim.artbookkotlin.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class ActivityDetails : AppCompatActivity() {
    private lateinit var binding:ActivityDetailsBinding

    private lateinit var actvitiyResultLauncher:ActivityResultLauncher<Intent>
    private lateinit var permissonLauncher: ActivityResultLauncher<String>
    private lateinit var database:SQLiteDatabase

    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        registerLauncher()
        val database=openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        val intent=intent
        val info=intent.getStringExtra("info")//eğer info new e eşitese eğer old a eşitse...
        if(info.equals("new")){
            binding.editTextText.setText("")
            binding.editTextText2.setText("")
            binding.editTextText3.setText("")
            binding.button.visibility=View.VISIBLE
            binding.imageView.setImageResource(R.drawable.indir)
        }else{

            binding.button.visibility=View.INVISIBLE

            val selectedId=intent.getIntExtra("id",1)
            val cursor=database.rawQuery("SELECT * FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            //eğer tek soru isareti varsa böyle de yapabilirisin
            val artnameIx=cursor.getColumnIndex("artname")
            val artistnameIx=cursor.getColumnIndex("artistname")
            val yearIx=cursor.getColumnIndex("year")
            val imageIx=cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                binding.editTextText.setText(cursor.getString(artnameIx))
                binding.editTextText2.setText(cursor.getString(artistnameIx))
                binding.editTextText3.setText(cursor.getString(yearIx))

                val byteArray=cursor.getBlob(imageIx)//***************************
                val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)//******************
                binding.imageView.setImageBitmap(bitmap)//********************
            }
            cursor.close()

        }
    }
    private fun makeSmallerImage(image: Bitmap, maximumSize: Int): Bitmap {
        var height = image.height.toFloat()
        var weidth = image.width.toFloat()
        val bitmapRadio = weidth as Float / height as Float
        if (bitmapRadio > 1) {
            weidth = maximumSize.toFloat()
            height = (weidth / bitmapRadio).toInt().toFloat()
        } else {
            height = maximumSize.toFloat()
            weidth = (height * bitmapRadio).toInt().toFloat()
        }

        return Bitmap.createScaledBitmap(image, weidth.toInt(), height.toInt(), true)
    }
    fun kaydet(view: View){
        val artname=binding.editTextText.text.toString()
        val artistname=binding.editTextText2.text.toString()
        val year=binding.editTextText3.text.toString()

        if(selectedBitmap!=null){
            val smallBitmap=makeSmallerImage(selectedBitmap!!,300)

            val outputsteream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputsteream)
            val bytearray=outputsteream.toByteArray()
            //bu üçü görseli 0 1 lere dönüştürür

            try {
                val database=openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY , artname VARCHAR, artistname VARCHAR, year VARCHAR ,image BLOB ) ")
                val SQLstring="INSERT INTO arts (artname,artistname,year,image) VALUES (?,?,?,?)"
                val statament=database.compileStatement(SQLstring)
                statament.bindString(1,artname)
                statament.bindString(2,artistname)
                statament.bindString(3,year)
                statament.bindBlob(4,bytearray)//byte arraye dönüştürmüştük
                statament.execute()

            }catch (e:Exception){
                e.printStackTrace()
            }
            val intent=Intent(this@ActivityDetails,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }
    fun image (view:View){
        //izin yoksa if true döndürür yani ilk başta hep if in içine girecek
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED){
            //izizni reddederse bu if in içine girer ama eğer kabul ederse aşağıdaki else girecek
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"permisson for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permisson",View.OnClickListener {
                    //izin alma
                    //eğer snacbardaki buton abasarsa izin alcaz
                    permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                })
            }else{ //buraya girerse izin yoktur demekki o yüzden izin alcaz
                permissonLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{  //buraya girerse zaten izini almıştır
            val intenttoGallery= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //intent
            actvitiyResultLauncher.launch(intenttoGallery)
        }
    }

    private fun registerLauncher(){
        actvitiyResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode== RESULT_OK){
                val intentFromResult=result.data
                if(intentFromResult!=null){
                    val imageData=intentFromResult.data
                    if(imageData!=null){
                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                val source=ImageDecoder.createSource(this@ActivityDetails.contentResolver,imageData)
                                selectedBitmap=ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap=MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                                //bitmapi imageviewe yüklüyoruz
                                //ve gösteriyoruz
                            }


                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                }
            }
        }
        permissonLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result){
                val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                actvitiyResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@ActivityDetails,"Permisson Needed",Toast.LENGTH_LONG).show()
            }

        }
    }
}