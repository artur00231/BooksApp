package com.booksapp.lists

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.booksapp.R
import com.booksapp.databinding.FragmentBookListFilterBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import java.io.Serializable

class BookListFilter : DialogFragment() {
    @Parcelize
    data class BookFilterData(var title: String, var author: String) : Parcelable

    private var returnCallback : ((BookFilterData) -> Unit)? = null
    private var filterData: BookFilterData = BookFilterData("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            bundle.getParcelable<BookFilterData>("filter")?.let {
                filterData = it
            }
        }

        //Load saved instance
        if (savedInstanceState != null) {
            savedInstanceState.getParcelable<BookFilterData>("filter")?.let {
                filterData = it
            }

            savedInstanceState.getSerializable("callback")?.let {
                returnCallback = it as (BookFilterData) -> Unit
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book_list_filter, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("filter", filterData)
        outState.putSerializable("callback", returnCallback as Serializable)

        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val b = MaterialAlertDialogBuilder(requireContext())
        val layoutInflater = requireActivity().layoutInflater
        val dialogBinding = FragmentBookListFilterBinding.inflate(layoutInflater)

        dialogBinding.filterAuthorLayout.editText?.setText(filterData.author)
        dialogBinding.filterTitleLayout.editText?.setText(filterData.title)

        b.setView(dialogBinding.root)
            .setPositiveButton("Apply") { _, _ ->
                returnCallback?.let {
                    it(BookFilterData(dialogBinding.filterTitle.text.toString(), dialogBinding.filterAuthor.text.toString())) }
                dismiss()
            }
            .setNegativeButton("Reset") { _, _ ->
                returnCallback?.let {
                    it(BookFilterData("", "")) }
                dismiss()
            }
            .setNeutralButton("Cancel") { _, _ ->
                dismiss()
            }

        return b.create()
    }

    fun setCallback(callback : (BookFilterData) -> Unit): BookListFilter {
        returnCallback = callback
        return this
    }

    companion object {
        @JvmStatic
        fun newInstance(filterData: BookFilterData) =
            BookListFilter().apply {
                arguments = Bundle().apply {
                    putParcelable("filter", filterData)
                }
            }

        const val TAG = "BookListFilter"
    }
}