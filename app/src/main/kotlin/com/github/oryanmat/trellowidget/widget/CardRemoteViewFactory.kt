package com.github.oryanmat.trellowidget.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.*
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.model.BoardList
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.model.Label
import com.github.oryanmat.trellowidget.util.DateTimeUtil
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImage
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImageViewColor
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setTextView
import com.github.oryanmat.trellowidget.util.TrelloAPIUtil
import com.github.oryanmat.trellowidget.util.color.colors
import com.github.oryanmat.trellowidget.util.color.dim
import com.github.oryanmat.trellowidget.util.getCardForegroundColor
import com.github.oryanmat.trellowidget.util.getSelectedLists
import java.util.*
import kotlin.collections.ArrayList

class CardRemoteViewFactory(private val context: Context,
                            private val appWidgetId: Int) : RemoteViewsService.RemoteViewsFactory {
    private var cards: ArrayList<Card> = ArrayList()
    private var listNames: ArrayList<String> = ArrayList()
    private var hasMoreThanOneList: Boolean = false
    @ColorInt private var color = 0

    override fun onDataSetChanged() {
        cards = ArrayList()
        listNames = ArrayList()
        val lists = context.getSelectedLists(appWidgetId)
        var numLists = 0
        for(l in lists.lists) {
            val list = TrelloAPIUtil.instance.getCards(l)
            color = context.getCardForegroundColor()

            if (BoardList.ERROR != list.id) {
                numLists += 1
                for(c in list.cards){
                    listNames.add(list.name)
                }
                cards.addAll(list.cards)
            } else {
                color = color.dim()
            }
        }
        hasMoreThanOneList = numLists > 1
    }

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards[position]
        val listName = listNames[position]
        val views = RemoteViews(context.packageName, R.layout.card)
        setLabels(views, card)
        setTitle(views, card)
        setBadges(views, card)
        setDivider(views)
        setOnClickFillInIntent(views, card)
        if(hasMoreThanOneList) setListName(views, listName) else setListName(views, "")
        return views
    }

    private fun setListName(views: RemoteViews, listName: String){
        setTextView(context, views, R.id.card_list_name, listName, color, R.dimen.card_badges_text)
    }

    private fun setOnClickFillInIntent(views: RemoteViews, card: Card) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(card.url))
        views.setOnClickFillInIntent(R.id.card, intent)
    }

    private fun setBadges(views: RemoteViews, card: Card) {
        setSubscribed(views, card)
        setVotes(views, card)
        setDueDate(views, card)
        setDescription(views, card)
        setComments(views, card)
        setChecklist(views, card)
        setAttachments(views, card)
    }

    private fun setTitle(views: RemoteViews, card: Card) {
        setTextView(context, views, R.id.card_title, card.name, color, R.dimen.card_badges_text)
    }

    private fun setSubscribed(views: RemoteViews, card: Card) {
        setBadge(views, R.id.subscribed, R.drawable.ic_visibility_white_24dp, card.badges.subscribed)
    }

    private fun setVotes(views: RemoteViews, card: Card) {
        setVisibility(views, R.id.votes_layout, card.badges.viewingMemberVoted)
        setIntBadge(views, R.id.votes, R.id.vote_count,
                R.drawable.ic_thumb_up_white_24dp, card.badges.votes)
    }

    private fun setDescription(views: RemoteViews, card: Card) {
        setBadge(views, R.id.desc, R.drawable.ic_subject_white_24dp, card.badges.description)
    }

    private fun setDueDate(views: RemoteViews, card: Card) {
        val visible = card.badges.due != null
        val text = if (visible) DateTimeUtil.parseDate(card.badges.due!!) else ""
        setBadge(views, R.id.due, R.id.due_string,
                R.drawable.ic_access_time_white_24dp, text, visible)
    }

    private fun setChecklist(views: RemoteViews, card: Card) {
        val text = "${card.badges.checkItemsChecked}/${card.badges.checkItems}"
        val visible = card.badges.checkItems > 0
        setBadge(views, R.id.checklist, R.id.checklist_count,
                R.drawable.ic_check_box_white_24dp, text, visible)
    }

    private fun setComments(views: RemoteViews, card: Card) {
        setIntBadge(views, R.id.comments, R.id.comment_count,
                R.drawable.ic_chat_bubble_outline_white_24dp, card.badges.comments)
    }

    private fun setAttachments(views: RemoteViews, card: Card) {
        setIntBadge(views, R.id.attachment, R.id.attachment_count,
                R.drawable.ic_attachment_white_24dp, card.badges.attachments)
    }

    private fun setIntBadge(views: RemoteViews, @IdRes view: Int, @IdRes textView: Int,
                            @DrawableRes image: Int, value: Int) {
        setBadge(views, view, textView, image, value.toString(), value > 0)
    }

    private fun setBadge(views: RemoteViews, @IdRes view: Int, @IdRes textView: Int,
                         @DrawableRes image: Int, text: String, visible: Boolean) {
        setTextView(context, views, textView, text, color, R.dimen.card_badges_text)
        setVisibility(views, textView, visible)
        setBadge(views, view, image, visible)
    }

    private fun setBadge(views: RemoteViews, @IdRes view: Int, @DrawableRes image: Int, visible: Boolean) {
        setVisibility(views, view, visible)
        setImageViewColor(views, view, color)
        setImage(context, views, view, image)
    }

    private fun setLabels(views: RemoteViews, card: Card) {
        views.removeAllViews(R.id.labels_layout)
        card.labels.forEach { setLabel(views, it) }
    }

    private fun setLabel(views: RemoteViews, label: Label) {
        var labelColor: Int = colors[label.color] ?: Color.TRANSPARENT
        labelColor = Color.argb(alpha(color), red(labelColor), green(labelColor), blue(labelColor))
        val view = RemoteViews(context.packageName, R.layout.label)
        setImageViewColor(view, R.id.label, labelColor)
        setImage(context, view, R.id.label, R.drawable.label)
        views.addView(R.id.labels_layout, view)
    }

    private fun setDivider(views: RemoteViews) = setImageViewColor(views, R.id.list_item_divider, color)

    private fun setVisibility(views: RemoteViews, @IdRes view: Int, visible: Boolean) =
            views.setViewVisibility(view, if (visible) View.VISIBLE else View.GONE)

    override fun onCreate() {
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int = cards.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds() = true
}