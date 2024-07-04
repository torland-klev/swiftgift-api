package klev.db.groups

import klev.db.wishes.Wishes
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object GroupsToWishes : IntIdTable() {
    val groupId = integer("groupId").references(Groups.id, onDelete = ReferenceOption.CASCADE)
    val wishId = integer("wishId").references(Wishes.id, onDelete = ReferenceOption.CASCADE)
    val created = timestamp("created").defaultExpression(CurrentTimestamp())
    val updated = timestamp("updated").defaultExpression(CurrentTimestamp())
}
