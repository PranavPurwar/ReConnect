package dev.pranav.reconnect.ui.journey

import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.storage.ContactStore
import dev.pranav.reconnect.core.storage.MomentStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FakeContactStore: ContactStore {
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    override val contacts: StateFlow<List<Contact>> = _contacts

    fun setMoments(contacts: List<Contact>) {
        _contacts.value = contacts
    }

    override suspend fun addContacts(newContacts: List<Contact>) {
        _contacts.value = _contacts.value + newContacts
    }

    override suspend fun addContact(contact: Contact) {
        _contacts.value = _contacts.value + contact
    }

    override suspend fun updateContact(contact: Contact) {
        _contacts.value = _contacts.value.map { if (it.id == contact.id) contact else it }
    }

    override suspend fun deleteContact(contactId: String) {
        _contacts.value = _contacts.value.filterNot { it.id == contactId }
    }

    override suspend fun findById(contactId: String): Contact? {
        return _contacts.value.find { it.id == contactId }
    }
}

class FakeMomentStore: MomentStore {
    private val _moments = MutableStateFlow<List<PastMoment>>(emptyList())
    override val moments: StateFlow<List<PastMoment>> = _moments

    fun setMoments(moments: List<PastMoment>) {
        _moments.value = moments
    }

    override suspend fun addMoment(moment: PastMoment) {
        _moments.value = _moments.value + moment
    }

    override suspend fun getMomentsFor(contactId: String): List<PastMoment> {
        return _moments.value.filter { it.contactIds.contains(contactId) }
    }

    override suspend fun deleteMomentsForContact(contactId: String) {
        _moments.value = _moments.value.filterNot { it.contactIds.contains(contactId) }
    }
}

class JourneyViewModelTest {

    private lateinit var contactStore: FakeContactStore
    private lateinit var momentStore: FakeMomentStore
    private lateinit var viewModel: JourneyViewModel

    @Before
    fun setUp() {
        contactStore = FakeContactStore()
        momentStore = FakeMomentStore()
        viewModel = JourneyViewModel(contactStore, momentStore)
    }

    @Test
    fun testEmptyStateWhenNoMoments() {
        val state = viewModel.uiState.value
        assertEquals(emptyList<JourneyItem>(), state.filteredItems)
        assertNull(state.selectedCategory)
    }

    @Test
    fun testMomentsWithMissingContactsAreFiltered() {
        momentStore.setMoments(
            listOf(
                PastMoment(
                    id = "m1",
                    contactIds = listOf("c1"),
                    title = "Dinner",
                    description = "",
                    dateEpochMs = 1000L,
                    category = MomentCategory.DINING
                )
            )
        )

        val state = viewModel.uiState.value
        assertEquals(emptyList<JourneyItem>(), state.filteredItems)
    }

    @Test
    fun testMomentsLoadWhenContactsExist() {
        contactStore.setMoments(
            listOf(
                Contact(id = "c1", name = "Alice"),
                Contact(id = "c2", name = "Bob")
            )
        )
        momentStore.setMoments(
            listOf(
                PastMoment(
                    id = "m1",
                    contactIds = listOf("c1", "c2"),
                    title = "Lunch Together",
                    description = "",
                    dateEpochMs = 1000L,
                    category = MomentCategory.DINING
                )
            )
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredItems.size)
        assertEquals("Alice, Bob", state.filteredItems[0].contactNames)
        assertEquals("Lunch Together", state.filteredItems[0].moment.title)
    }

    @Test
    fun testFilterByCategoryToggle() {
        contactStore.setMoments(listOf(Contact(id = "c1", name = "Alice")))
        momentStore.setMoments(
            listOf(
                PastMoment(
                    id = "m1",
                    contactIds = listOf("c1"),
                    title = "Dinner",
                    description = "",
                    dateEpochMs = 1000L,
                    category = MomentCategory.DINING
                ),
                PastMoment(
                    id = "m2",
                    contactIds = listOf("c1"),
                    title = "Gallery Visit",
                    description = "",
                    dateEpochMs = 2000L,
                    category = MomentCategory.ART
                )
            )
        )

        assertEquals(2, viewModel.uiState.value.filteredItems.size)

        viewModel.setFilter(MomentCategory.DINING)
        assertEquals(1, viewModel.uiState.value.filteredItems.size)
        assertEquals("m1", viewModel.uiState.value.filteredItems[0].moment.id)

        viewModel.setFilter(MomentCategory.DINING)
        assertEquals(2, viewModel.uiState.value.filteredItems.size)
    }

    @Test
    fun testMultipleContactsMergedIntoSingleString() {
        contactStore.setMoments(
            listOf(
                Contact(id = "c1", name = "Alice"),
                Contact(id = "c2", name = "Bob"),
                Contact(id = "c3", name = "Charlie")
            )
        )
        momentStore.setMoments(
            listOf(
                PastMoment(
                    id = "m1",
                    contactIds = listOf("c1", "c2", "c3"),
                    title = "Group Outing",
                    description = "",
                    dateEpochMs = 1000L,
                    category = MomentCategory.OUTDOORS
                )
            )
        )

        val item = viewModel.uiState.value.filteredItems[0]
        assertEquals("Alice, Bob, Charlie", item.contactNames)
    }
}




