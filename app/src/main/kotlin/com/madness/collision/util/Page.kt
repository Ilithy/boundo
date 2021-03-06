/*
 * Copyright 2020 Clifford Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.madness.collision.util

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.madness.collision.Democratic
import com.madness.collision.R
import com.madness.collision.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_page.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Suppress("FunctionName")
inline fun <reified T: Fragment> Page(titleId: Int = 0, democratic: Democratic? = null): Page {
    return Page(T::class.createInstance(), titleId, democratic)
}

@Suppress("FunctionName")
inline fun <reified T: Fragment> TypedNavArg(): TypedNavArg {
    return TypedNavArg().apply {
        clazz = T::class
    }
}

class TypedNavArg() : Parcelable {

    private var _clazz: Class<out Fragment>? = null
    var clazz: KClass<out Fragment>? = null
        get() = field ?: _clazz?.kotlin

    @Suppress("UNCHECKED_CAST")
    constructor(parcel: Parcel) : this() {
        _clazz = parcel.readSerializable() as Class<Fragment>?
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(_clazz)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TypedNavArg> {
        override fun createFromParcel(parcel: Parcel): TypedNavArg {
            return TypedNavArg(parcel)
        }

        override fun newArray(size: Int): Array<TypedNavArg?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * Wrap a fragment
 */
class Page(fragment: Fragment? = null, private var titleId: Int = 0, private val democratic: Democratic? = null) : Fragment(), Democratic {

    private val mainViewModel: MainViewModel by activityViewModels()
    private var mFragment: Fragment? = null
    init {
        if (fragment != null) {
            mFragment = fragment
        }
    }

    override fun createOptions(context: Context, toolbar: Toolbar, iconColor: Int): Boolean {
        if (democratic != null) {
            return democratic.createOptions(context, toolbar, iconColor)
        }
        if (titleId != 0) {
            toolbar.setTitle(titleId)
        }
        return true
    }

    override fun selectOption(item: MenuItem): Boolean {
        if (democratic != null) {
            return democratic.selectOption(item)
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // nav args
        val args = arguments ?: return
        val fragmentClass = args.getParcelable<TypedNavArg>("fragmentClass")
        if (fragmentClass != null) {
            mFragment = fragmentClass.clazz?.createInstance()
        }
        titleId = args.getInt("titleId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val f = mFragment ?: return
        ensureAdded(R.id.pageContainer, f)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        democratize(mainViewModel)
        mainViewModel.contentWidthTop.observe(viewLifecycleOwner) {
            pageContainer.alterPadding(top = it)
        }
        mainViewModel.contentWidthBottom.observe(viewLifecycleOwner) {
            pageContainer.alterPadding(bottom = it)
        }
    }

}
