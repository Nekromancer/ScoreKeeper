package pl.ownvision.scorekeeper.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

/**
 * Created by jakub on 31.05.2017.
 */

open class Player (
        @PrimaryKey
        var id: String = UUID.randomUUID().toString(),

        var createdAt: java.util.Date = Date(),

        @Required
        var name: String = ""
) : RealmObject()