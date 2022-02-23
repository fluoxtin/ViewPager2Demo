package com.example.viewpager2demo.view


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.viewpager2demo.R
import com.example.viewpager2demo.adapter.DefaultAvatarAdapter
import com.example.viewpager2demo.helper.ViewPager2SlowScrollHelper
import com.example.viewpager2demo.utils.dp2px


/**
 *  @author created by LiWenqing on 2022/2/17
 */

class DefaultAvatarViewPager : RelativeLayout , DefaultAvatarAdapter.OnItemClickedListener{

    companion object {
        const val TAG = "DefaultAvatarViewPager"
    }

    lateinit var viewPager2: ViewPager2

    lateinit var scrollHelper : ViewPager2SlowScrollHelper

    private var mAdapter : DefaultAvatarAdapter? = null

    private var marginPageTransformer : MarginPageTransformer? = null

    private var mCompositePageTransformer: CompositePageTransformer


    var looperEnable = true

    var userInputEnable = true

    var pagerMargin = 0

    var listSize = 0

    var offscreenPageLimit = 3

    var revealWidth = dp2px(147)

    var resetIndex = 0

    private var pageMargin = 0

    private var slowScrollDuration : Int = 0

    private var currentPosition = 0


    private val mOnPagerChangedCallBack : ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPosition = position
            Log.d(TAG,currentPosition.toString());
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr : Int) : super(context, attributes, defStyleAttr)

    init {
        initView()
        mCompositePageTransformer = CompositePageTransformer()
        viewPager2.setPageTransformer(mCompositePageTransformer)

    }

    private fun initView() {
        inflate(context, R.layout.register_vp_layout, this)
        viewPager2 = findViewById(R.id.view_pager2)
        scrollHelper = ViewPager2SlowScrollHelper(viewPager2)
    }

    private fun setupViewPager() {
        if (mAdapter == null)
            throw NullPointerException("u must set adapter first")

        if (revealWidth != -1) {
            val recyclerView = viewPager2.getChildAt(0) as RecyclerView
            recyclerView.setPadding(revealWidth, dp2px(14), revealWidth, dp2px(14))
            recyclerView.clipToPadding = false
            recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        viewPager2.adapter = mAdapter

        resetCurrentItem()

        viewPager2.offscreenPageLimit = offscreenPageLimit
        viewPager2.unregisterOnPageChangeCallback(mOnPagerChangedCallBack)
        viewPager2.registerOnPageChangeCallback(mOnPagerChangedCallBack)
        viewPager2.isUserInputEnabled = userInputEnable
    }

    private fun resetCurrentItem() {
        if (listSize > 0 && looperEnable) {
            scrollHelper.setCurrentItem(
                Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % listSize) + resetIndex
            )
        } else
            viewPager2.setCurrentItem(0, false)
    }

    private fun removeMarginPageTransformer() {
        if (marginPageTransformer != null)
            mCompositePageTransformer.removeTransformer(marginPageTransformer!!)
    }

    // 通过反射试图再更新一次 currentView
    fun updateCurrentItem() {
        val updateCurrentItemMethod = viewPager2.javaClass.getDeclaredMethod("updateCurrentItem")
        updateCurrentItemMethod.isAccessible = true
        updateCurrentItemMethod.invoke(viewPager2)
    }

    fun getCurrentPosition() : Int {
        updateCurrentItem()
        return mAdapter!!.getRealPosition(viewPager2.currentItem)
    }

    /**
     *  用于绑定数据
     */
    fun bind(data : List<Any>) {
        if (mAdapter == null)
            throw NullPointerException("u must set adapter first")
        listSize = data.size
        mAdapter!!.setEntities(data)
        mAdapter!!.looperEnable = looperEnable
        setupViewPager()
    }

    /**
     *  设置 adapter
     */
    fun setAdapter(adapter: DefaultAvatarAdapter?) {
        mAdapter = adapter
        mAdapter?.setOnItemClickedListener(this)
    }

    fun setPageMargin(margin : Int) {
        pageMargin = dp2px(margin)
        marginPageTransformer = MarginPageTransformer(pageMargin)
        removeMarginPageTransformer()
        mCompositePageTransformer.addTransformer(marginPageTransformer!!)
    }

    fun addPageTransformer(pageTransformer : ViewPager2.PageTransformer) {
        mCompositePageTransformer.addTransformer(pageTransformer)
    }

    fun removePageTransformer(pageTransformer: ViewPager2.PageTransformer) {
        mCompositePageTransformer.removeTransformer(pageTransformer)
    }

    fun setSlowScrollDuration(duration : Int) {
        slowScrollDuration = duration
        scrollHelper.setDuration(slowScrollDuration)
    }


    // 点击过快会出现无效情况 仅针对允许手动滑动的情况，因为之前动态调整 view 宽度的现在
    override fun onItemClicked(position : Int) {
        scrollHelper.setCurrentItem(position)

    }

}