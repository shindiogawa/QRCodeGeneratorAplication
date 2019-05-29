package aplication.ogawadev.com.qrcodegenerate.manager

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.fragment.GeradorFragment
import aplication.ogawadev.com.qrcodegenerate.fragment.LeitorFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    //var listFragmentId = ArrayList<Int>()
    var doubleBackToexitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_main, null)
        setContentView(view)
        navMenu = nav

        listFragmentId.add(R.id.itemLer)
        setFragment(1, this)
        nav.selectedItemId = R.id.itemLer

        nav.setOnNavigationItemSelectedListener { item ->

            when(item.itemId){

                R.id.itemLer -> {
                    //listFragmentId.add(R.id.itemLer)
                    setFragment(1, this)
                }


                R.id.itemGerar ->{
                    //listFragmentId.add(R.id.itemGerar)
                    setFragment(0, this)
                }

            }
            true

        }
    }


    override fun onBackPressed() {
        var backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount == 1){
            if (doubleBackToexitPressedOnce) {
                finish()
            }

            this.doubleBackToexitPressedOnce = true
            Toast.makeText(this, "Aperte voltar de novo para sair", Toast.LENGTH_SHORT).show()

            Handler().postDelayed( { doubleBackToexitPressedOnce = false }, 2000)
        }else{

            var fragmentId = listFragmentId.get(backStackEntryCount-2)
            when(fragmentId){
                R.id.itemLer -> {
                    nav.selectedItemId = R.id.itemLer
                    super.onBackPressed()
                }

                R.id.itemGerar -> {
                    nav.selectedItemId = R.id.itemGerar
                    super.onBackPressed()
                }

            }
            super.onBackPressed()
        }
  }

    companion object{
        var listFragmentId = ArrayList<Int>()

        val geradorFragment = GeradorFragment()
        val leitorFragment = LeitorFragment()
        var fragmentTransacion : FragmentTransaction? = null
        var navMenu: BottomNavigationView? = null

        fun setFragment(fragmentInt: Int, context: Context){
            var fragment: Fragment? = null
            var fragmentId = 0

            if(fragmentInt == 0) {
                fragment = geradorFragment
                fragmentId = R.id.itemGerar
                //navMenu!!.selectedItemId = R.id.itemGerar
            }
            if(fragmentInt == 1) {
                fragment = leitorFragment
                fragmentId = R.id.itemLer
                //navMenu!!.selectedItemId = R.id.itemLer
            }
            if(fragment != null){
                listFragmentId.add(fragmentId)
                fragmentTransacion = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                fragmentTransacion!!.replace(R.id.frame, fragment).addToBackStack(null)
                fragmentTransacion!!.commit()
            }
        }
    }

}