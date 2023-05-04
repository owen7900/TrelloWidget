package com.github.oryanmat.trellowidget.activity

import android.app.Activity
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.VolleyError
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.Board
import com.github.oryanmat.trellowidget.model.BoardList
import com.github.oryanmat.trellowidget.model.BoardList.Companion.BOARD_LIST_TYPE
import com.github.oryanmat.trellowidget.model.BoardListList
import com.github.oryanmat.trellowidget.util.*
import com.github.oryanmat.trellowidget.widget.updateWidget
import kotlinx.android.synthetic.main.activity_config.*

class ConfigActivity : Activity(), OnItemSelectedAdapter, Response.Listener<String>, Response.ErrorListener, OnItemClickListener {
    private var appWidgetId = INVALID_APPWIDGET_ID
    private var board: Board = Board()
    private var lists: BoardListList = BoardListList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        setWidgetId()
        get(TrelloAPIUtil.instance.boards(), this)
    }

    private fun setWidgetId() {
        val extras = intent.extras

        if (extras != null) {
            appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        if (appWidgetId == INVALID_APPWIDGET_ID) {
            finish()
        }
    }

    private fun get(url: String, listener: ConfigActivity) =
            TrelloAPIUtil.instance.getAsync(url, listener, listener)

    override fun onResponse(response: String) {
        progressBar.visibility = View.GONE
        content.visibility = View.VISIBLE
        val boards = Json.tryParseJson(response, BOARD_LIST_TYPE, emptyList<Board>())
        board = getBoard(appWidgetId)
        setSpinner(boardSpinner, boards, this, boards.indexOf(board))
    }

    override fun onErrorResponse(error: VolleyError) {
        finish()

        Log.e(T_WIDGET, error.toString())
        val text = getString(R.string.board_load_fail)
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent) {
            boardSpinner -> boardSelected(parent, position)
        }
    }

    private fun boardSelected(spinner: AdapterView<*>, position: Int) {
        board = spinner.getItemAtPosition(position) as Board
        lists = getSelectedLists(appWidgetId)
        var selectedIdxs = ArrayList<Int>()

        for(i in board.lists.indices){
            if(lists.lists.contains(board.lists[i])){
                selectedIdxs.add(i)
            }
        }

        setView(listListView, board.lists, this, selectedIdxs)
    }

    private fun <T> setView(listView: ListView, lists: List<T>, listener: AdapterView.OnItemClickListener, selectedIndexes: List<Int>): ListView{
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_multichoice, lists)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        listView.adapter = adapter
        listView.onItemClickListener = listener
        for(i in lists.indices){
            listView.setItemChecked(i, false)
        }
        for(i in selectedIndexes)
        {
            listView.setItemChecked(i, true)
        }
        return listView
    }

    override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (parent){
            listListView -> {
                var list = ArrayList<BoardList>()
                for(i in board.lists.indices){
                    if(listListView.isItemChecked(i)) {
                        list.add(board.lists[i])
                    }
                }
                lists = BoardListList(lists = list)
            }
        }
    }

    private fun <T> setSpinner(spinner: Spinner, lists: List<T>,
                               listener: AdapterView.OnItemSelectedListener, selectedIndex: Int): Spinner {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lists)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
        spinner.setSelection(if (selectedIndex > -1) selectedIndex else 0)
        return spinner
    }

    fun ok(view: View) {

        if (board.id.isEmpty() || lists.lists.isEmpty()) {
            Log.println(Log.ERROR, "Ok Failed", "BoardID: " + board.id + " ListsSize: " + lists.lists.size.toString())
            return
        }
        putConfigInfo(appWidgetId, board, lists)
        updateWidget(appWidgetId)
        returnOk()
    }

    private fun returnOk() {
        val resultValue = Intent()
        resultValue.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    fun cancel(view: View) = finish()
}