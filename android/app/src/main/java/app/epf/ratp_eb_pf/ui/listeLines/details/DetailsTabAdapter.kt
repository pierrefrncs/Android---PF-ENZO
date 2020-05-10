package app.epf.ratp_eb_pf.ui.listeLines.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DetailsTabAdapter(fm: FragmentManager, data: Bundle) : FragmentPagerAdapter(fm) {


    private val fragmentList = arrayListOf<Fragment>()
    private val fragmentTitle = arrayListOf<String>()
    private val fragmentBundle: Bundle = data

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