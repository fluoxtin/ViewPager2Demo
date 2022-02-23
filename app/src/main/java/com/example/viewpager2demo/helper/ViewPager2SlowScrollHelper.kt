package com.example.viewpager2demo.helper

import android.content.Context
import android.util.DisplayMetrics
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.lang.reflect.Method

/**
 *  Description : viewPager2 辅助类，可以调节 ViewPager2 中 Item
 *               的滚动速度
 *
 *  @author LiWenqing on 2022/2/18
 *
 *  @param viewPager2 of ViewPager2
 */
class ViewPager2SlowScrollHelper(private val viewPager2: ViewPager2) {

    private var duration : Int = 0

    private val recyclerView : RecyclerView
    private val mAccessibilityProvider: Any
    private val mScrollEventAdapter: Any
    private val onSetNewCurrentItemMethod: Method
    private val getRelativeScrollPositionMethod: Method
    private val notifyProgrammaticScrollMethod: Method

    init {
        val mRecyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        mRecyclerViewField.isAccessible = true
        recyclerView = mRecyclerViewField.get(viewPager2) as RecyclerView
        val mAccessibilityProviderField =
            ViewPager2::class.java.getDeclaredField("mAccessibilityProvider")
        mAccessibilityProviderField.isAccessible = true
        mAccessibilityProvider = mAccessibilityProviderField.get(viewPager2)
        onSetNewCurrentItemMethod =
            mAccessibilityProvider.javaClass.getDeclaredMethod("onSetNewCurrentItem")
        onSetNewCurrentItemMethod?.isAccessible = true


        val mScrollEventAdapterField =
            ViewPager2::class.java.getDeclaredField("mScrollEventAdapter")
        mScrollEventAdapterField.isAccessible = true
        mScrollEventAdapter = mScrollEventAdapterField.get(viewPager2)
        getRelativeScrollPositionMethod =
            mScrollEventAdapter.javaClass.getDeclaredMethod("getRelativeScrollPosition")
        getRelativeScrollPositionMethod?.isAccessible = true

        notifyProgrammaticScrollMethod = mScrollEventAdapter.javaClass.getDeclaredMethod(
            "notifyProgrammaticScroll",
            Int::class.java,
            Boolean::class.java
        )
        notifyProgrammaticScrollMethod?.isAccessible = true
    }

    /**
     * 模拟手写 Viewpage2 的 setCurrentItemInternal(int item, boolean smoothScroll) 方法
     * 其中 smoothScroll 为 true
     * 主要目的是通过手动实现vp的翻页方法达到控制RecycleView执行滚动的SmoothScroller对象
     */
    fun setCurrentItem(position: Int) {
        var item = position
        val adapter: RecyclerView.Adapter<*> = viewPager2.adapter as RecyclerView.Adapter<*>
        if (adapter.itemCount <= 0) {
            return
        }
        item = item.coerceAtLeast(0)
        item = item.coerceAtMost(adapter.itemCount - 1)
        if (item == viewPager2.currentItem && viewPager2.scrollState == ViewPager2.SCROLL_STATE_IDLE) {
            return
        }
        if (item == viewPager2.currentItem) {
            return
        }
        viewPager2.currentItem = item
        onSetNewCurrentItemMethod.invoke(mAccessibilityProvider)
        notifyProgrammaticScrollMethod.invoke(mScrollEventAdapter, item, true)
        smoothScrollToPosition(item, viewPager2.context, recyclerView.layoutManager)
    }

    /**
     * 模拟手写 RecyclerView 的 smoothScrollToPosition 方法 替换了startSmoothScroll 的参数达到了改变速度的目的
     */
    private fun smoothScrollToPosition(
        item: Int,
        context: Context,
        layoutManager: RecyclerView.LayoutManager?
    ) {
        val linearSmoothScroller = getSlowLinearSmoothScroller(context)
        replaceDecelerateInterpolator(linearSmoothScroller)
        linearSmoothScroller.targetPosition = item
        layoutManager?.startSmoothScroll(linearSmoothScroller)
    }

    /**
     *  减速核心 SmoothScroller 对象
     *  super.calculateSpeedPerPixel(displayMetrics) * slowCoefficient 为速度放慢 slowCoefficient 倍
     *  既动画时长增加 slowCoefficient 倍
     */
    private fun getSlowLinearSmoothScroller(context: Context): RecyclerView.SmoothScroller {
        return object : LinearSmoothScroller(context) {
            /**
             * 按照sdk注释的内容理解这个方法的返回值为每个像素滚动的时间
             * 例如返回 1 则代表滚动1个像素需要1ms 既1920px的滚动距离 则需要滚动1.92s
             */
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return duration / (viewPager2.width.toFloat() * 3.0f)
            }
        }
    }

    /**
     * 修改 SmoothScroller 的默认差值器，将其改为线性输出，不然会影响后续的vp动画
     * 如果没有自定义动画可以不用这个方法
     */
    private fun replaceDecelerateInterpolator(linearSmoothScroller: RecyclerView.SmoothScroller) {
        val mDecelerateInterpolatorField =
            LinearSmoothScroller::class.java.getDeclaredField("mDecelerateInterpolator")
        mDecelerateInterpolatorField.isAccessible = true
        mDecelerateInterpolatorField.set(linearSmoothScroller, object : DecelerateInterpolator() {
            override fun getInterpolation(input: Float): Float {
                return input
            }
        })
    }

    fun setDuration(duration : Int) {
        this.duration = duration
    }

}