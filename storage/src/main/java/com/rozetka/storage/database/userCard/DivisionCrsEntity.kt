package com.rozetka.storage.database.userCard



import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "divisions_crs",
    foreignKeys = [
        ForeignKey(
            entity = UserStudentCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["userOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userOwnerId")]
)
data class DivisionCrsEntity(
    @PrimaryKey(autoGenerate = true)
    val divisionId: Long = 0,
    val userOwnerId: String,
    val id: String,
    val contact: String,
    val name: String
)