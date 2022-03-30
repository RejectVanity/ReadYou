package me.ash.reader.data.source

import android.content.Context
import be.ceau.opml.OpmlParser
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.*
import me.ash.reader.data.feed.Feed
import me.ash.reader.data.group.Group
import me.ash.reader.data.group.GroupWithFeed
import me.ash.reader.data.repository.StringsRepository
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class OpmlLocalDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val stringsRepository: StringsRepository,
) {
    fun getDefaultGroupId(): String {
        val readYouString = stringsRepository.getString(R.string.read_you)
        val defaultString = stringsRepository.getString(R.string.defaults)
        return context.dataStore
            .get(DataStoreKeys.CurrentAccountId)!!
            .spacerDollar(readYouString + defaultString)
    }

    //    @Throws(XmlPullParserException::class, IOException::class)
    fun parseFileInputStream(inputStream: InputStream, defaultGroup: Group): List<GroupWithFeed> {
        val accountId = context.currentAccountId
        val opml = OpmlParser().parse(inputStream)
        val groupWithFeedList = mutableListOf<GroupWithFeed>().also {
            it.addGroup(defaultGroup)
        }

        opml.body.outlines.forEach {
            // Only feeds
            if (it.subElements.isEmpty()) {
                // It's a empty group
                if (it.attributes["xmlUrl"] == null) {
                    if (!it.attributes["isDefault"].toBoolean()) {
                        groupWithFeedList.addGroup(
                            Group(
                                id = UUID.randomUUID().toString(),
                                name = it.attributes["title"] ?: it.text!!,
                                accountId = accountId,
                            )
                        )
                    }
                } else {
                    groupWithFeedList.addFeedToDefault(
                        Feed(
                            id = UUID.randomUUID().toString(),
                            name = it.attributes["title"] ?: it.text!!,
                            url = it.attributes["xmlUrl"]!!,
                            groupId = defaultGroup.id,
                            accountId = accountId,
                            isNotification = it.attributes["isNotification"].toBoolean(),
                            isFullContent = it.attributes["isFullContent"].toBoolean(),
                        )
                    )
                }
            } else {
                var groupId = defaultGroup.id
                if (!it.attributes["isDefault"].toBoolean()) {
                    groupId = UUID.randomUUID().toString()
                    groupWithFeedList.addGroup(
                        Group(
                            id = groupId,
                            name = it.attributes["title"] ?: it.text!!,
                            accountId = accountId,
                        )
                    )
                }
                it.subElements.forEach { outline ->
                    groupWithFeedList.addFeed(
                        Feed(
                            id = UUID.randomUUID().toString(),
                            name = outline.attributes["title"] ?: outline.text!!,
                            url = outline.attributes["xmlUrl"]!!,
                            groupId = groupId,
                            accountId = accountId,
                            isNotification = outline.attributes["isNotification"].toBoolean(),
                            isFullContent = outline.attributes["isFullContent"].toBoolean(),
                        )
                    )
                }
            }
        }
        return groupWithFeedList
    }

    private fun MutableList<GroupWithFeed>.addGroup(group: Group) {
        add(GroupWithFeed(group = group, feeds = mutableListOf()))
    }

    private fun MutableList<GroupWithFeed>.addFeed(feed: Feed) {
        last().feeds.add(feed)
    }

    private fun MutableList<GroupWithFeed>.addFeedToDefault(feed: Feed) {
        first().feeds.add(feed)
    }
}