package com.example.viewpager2demo.utils

import android.content.res.Resources


/**
 *  description : 屏幕相关的 utils 类，后续和屏幕相关的全局静态方法将补充在这里
 *
 */

fun getScreenDensity() : Float = Resources.getSystem().displayMetrics.density;

fun dp2px(dip : Int) : Int = (0.5 + dip * getScreenDensity()).toInt()
