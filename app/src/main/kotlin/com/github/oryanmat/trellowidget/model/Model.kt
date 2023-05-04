package com.github.oryanmat.trellowidget.model

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class Board(
        val id: String = "-1",
        val name: String = "",
        val url: String = "",
        val lists: List<BoardList> = emptyList()) {

    override fun toString() = name

    companion object {
        val NULL_JSON = """{"id":"-1","name":"oops","url":"","lists":[]}"""
    }
}

data class BoardList(
        val id: String = "-1",
        val name: String = "",
        val cards: List<Card> = emptyList()) {

    override fun toString() = name

    companion object {
        val BOARD_LIST_TYPE: Type = object : TypeToken<List<Board>>() {}.type
        val NULL_JSON = """{"id":"-1","name":"oops","cards":[]}"""
        val ERROR = "ERROR"

        fun error(name: String) = BoardList(id = ERROR, cards = listOf(Card(name = name)))
    }
}

data class BoardListList(
        val lists: List<BoardList> = emptyList()){
    override fun toString(): String {
        var ret = "Selected Boards: "
        for(b in lists){
            ret += "$b, "
        }
        return ret
    }

    companion object {
        val BOARD_LIST_LIST_TYPE : Type = object : TypeToken<List<BoardList>>() {}.type
        val NULL_JSON = """{"lists": []}"""

        fun error(name: String) = BoardListList()
    }
}

data class Card(
        val id: String = "",
        val name: String = "",
        val desc: String = "",
        val due: String = "",
        val badges: Badges = Badges(),
        val url: String = "",
        val labels: List<Label> = emptyList())

data class Badges(
        val votes: Int = 0,
        val viewingMemberVoted: Boolean = false,
        val subscribed: Boolean = false,
        val fogbugz: String = "",
        val checkItems: Int = 0,
        val checkItemsChecked: Int = 0,
        val comments: Int = 0,
        val attachments: Int = 0,
        val description: Boolean = false,
        val due: String? = "")

data class Label(
        val id: String = "",
        val idBoard: String = "",
        val name: String = "",
        val color: String = "",
        val uses: Int = 0)

data class User(
        val id: String = "",
        val fullName: String = "",
        val username: String = "") {
    override fun toString() = "$fullName (@$username)"
}