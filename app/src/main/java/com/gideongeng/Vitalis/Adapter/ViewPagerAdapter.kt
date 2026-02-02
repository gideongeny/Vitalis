package com.gideongeng.Vitalis.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter : FragmentPagerAdapter {
    private val fragmentList1: ArrayList<Fragment> = ArrayList()
    private val fragmentTitleList1: ArrayList<String> = ArrayList()

    public constructor(supportFragmentManager: FragmentManager)
            : super(supportFragmentManager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

    override fun getItem(position: Int): Fragment {
        return fragmentList1.get(position)
    }
    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList1.get(position)
    }
    override fun getCount(): Int {
        return fragmentList1.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList1.add(fragment)
        fragmentTitleList1.add(title)
    }


}
