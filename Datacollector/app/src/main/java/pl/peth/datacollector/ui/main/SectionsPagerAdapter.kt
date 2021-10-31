package pl.peth.datacollector.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import pl.peth.datacollector.R
import pl.peth.datacollector.api.APIHandler

private val TAB_TITLES = arrayOf(
    R.string.tab_text_sensor,
    R.string.tab_text_position,
    R.string.tab_text_3
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager, apiHandler: APIHandler) : FragmentPagerAdapter(fm) {
    public var apiHandler: APIHandler = apiHandler

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        var fragment: Fragment = PlaceholderFragment.newInstance(position + 1)
        when (position) {
            0 -> {
                return SensorFragment(this)
            }
            1 -> {
                return PositionFragment()
            }
            2 -> {
                return PlaceholderFragment.newInstance(position + 1)
            }
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 3
    }
}