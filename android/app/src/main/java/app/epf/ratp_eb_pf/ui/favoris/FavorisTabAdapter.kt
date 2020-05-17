package app.epf.ratp_eb_pf.ui.favoris

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// Adapter pour configurer le ViewPager des favoris

class FavorisTabAdapter(fm: FragmentManager, data: Bundle) : FragmentPagerAdapter(fm) {


    private val fragmentList = arrayListOf<Fragment>()
    private val fragmentTitle = arrayListOf<String>()
    private val fragmentBundle: Bundle = data // Bundle permet de transférer les données du fragment principal vers les sous-fragments

    override fun getItem(position: Int): Fragment {
        fragmentList[position].arguments = fragmentBundle
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentTitle[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(frag: Fragment, title: String) {
        fragmentList.add(frag)
        fragmentTitle.add(title)
    }
}