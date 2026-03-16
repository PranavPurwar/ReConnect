package dev.pranav.reconnect.data.local.db

import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.PastMoment

fun ContactEntity.toModel(): Contact = Contact(
    id = id,
    name = name,
    title = title,
    relationship = relationship,
    notes = notes,
    photoUri = photoUri,
    seedColorArgb = seedColorArgb,
    phoneNumber = phoneNumber,
    isActive = isActive,
    isImportant = isImportant,
    reconnectInterval = reconnectInterval,
    birthdayMonth = birthdayMonth,
    birthdayDay = birthdayDay
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    title = title,
    relationship = relationship,
    notes = notes,
    photoUri = photoUri,
    seedColorArgb = seedColorArgb,
    phoneNumber = phoneNumber,
    isActive = isActive,
    isImportant = isImportant,
    reconnectInterval = reconnectInterval,
    birthdayMonth = birthdayMonth,
    birthdayDay = birthdayDay
)

fun MomentEntity.toModel(): PastMoment = PastMoment(
    id = id,
    contactId = contactId,
    title = title,
    description = description,
    dateLabel = dateLabel,
    category = category,
    imageUris = imageUris,
    createdAtEpochMs = createdAtEpochMs
)

fun PastMoment.toEntity(): MomentEntity = MomentEntity(
    id = id,
    contactId = contactId,
    title = title,
    description = description,
    dateLabel = dateLabel,
    category = category,
    imageUris = imageUris,
    createdAtEpochMs = createdAtEpochMs
)

