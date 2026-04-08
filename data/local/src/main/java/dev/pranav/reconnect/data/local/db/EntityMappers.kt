package dev.pranav.reconnect.data.local.db

import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.PastMoment

fun ContactEntity.toModel(): Contact = Contact(
    id = id,
    name = name,
    title = title,
    relationship = relationship,
    notes = notes,
    seedColorArgb = seedColorArgb,
    phoneNumber = phoneNumber,
    isActive = isActive,
    isImportant = isImportant,
    reconnectInterval = reconnectInterval,
    birthdayYear = birthdayYear,
    birthdayMonth = birthdayMonth,
    birthdayDay = birthdayDay
)

fun Contact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    title = title,
    relationship = relationship,
    notes = notes,
    seedColorArgb = seedColorArgb,
    phoneNumber = phoneNumber,
    isActive = isActive,
    isImportant = isImportant,
    reconnectInterval = reconnectInterval,
    birthdayYear = birthdayYear,
    birthdayMonth = birthdayMonth,
    birthdayDay = birthdayDay
)

fun MomentEntity.toModel(): PastMoment = PastMoment(
    id = id,
    contactIds = contactIds,
    title = title,
    description = description,
    dateEpochMs = dateEpochMs,
    category = category,
    images = images,
    isCoreMemory = isCoreMemory,
    wasPresent = wasPresent,
    groupName = groupName,
    locationMood = locationMood,
    createdAtEpochMs = createdAtEpochMs
)

fun PastMoment.toEntity(): MomentEntity = MomentEntity(
    id = id,
    contactIds = contactIds,
    title = title,
    description = description,
    dateEpochMs = dateEpochMs,
    category = category,
    images = images,
    isCoreMemory = isCoreMemory,
    wasPresent = wasPresent,
    groupName = groupName,
    locationMood = locationMood,
    createdAtEpochMs = createdAtEpochMs
)
