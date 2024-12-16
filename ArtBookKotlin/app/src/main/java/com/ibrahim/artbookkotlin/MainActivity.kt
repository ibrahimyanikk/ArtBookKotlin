package com.ibrahim.artbookkotlin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibrahim.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var ArtList:ArrayList<art>
    private lateinit var artadapter:ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ArtList=ArrayList<art>()

        artadapter=ArtAdapter(ArtList)
        binding.recyclerview.layoutManager=LinearLayoutManager(this)
        binding.recyclerview.adapter=artadapter
        try {
            //aşağıdakileri database de oluşan verileri çekmek için oluşturup kullanacağım
            val database=openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor=database.rawQuery("SELECT * FROM arts",null)
            val nameIx=cursor.getColumnIndex("artname")
            val IdIx=cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name=cursor.getString(nameIx)
                val id=cursor.getInt(IdIx)
                val Art=art(name,id)
                ArtList.add(Art)
            }
            artadapter.notifyDataSetChanged()//adapter güncellendi uyarısı yapar

            cursor.close()
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.menuu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this@MainActivity,ActivityDetails::class.java)
        intent.putExtra("info","new")
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }
}