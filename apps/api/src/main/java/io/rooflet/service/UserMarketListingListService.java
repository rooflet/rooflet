package io.rooflet.service;

import io.rooflet.model.entity.UserMarketListingList;
import io.rooflet.model.entity.UserMarketListingListItem;
import io.rooflet.repository.UserMarketListingListItemRepository;
import io.rooflet.repository.UserMarketListingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserMarketListingListService {

    private final UserMarketListingListRepository listRepository;
    private final UserMarketListingListItemRepository listItemRepository;

    // List CRUD operations
    public UserMarketListingList createList(UUID userId, String name, String description, String color) {
        UserMarketListingList list = UserMarketListingList.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .color(color)
                .build();
        return listRepository.save(list);
    }

    public UserMarketListingList updateList(UUID listId, String name, String description, String color) {
        UserMarketListingList list = listRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List not found with ID: " + listId));

        if (name != null) list.setName(name);
        if (description != null) list.setDescription(description);
        if (color != null) list.setColor(color);

        return listRepository.save(list);
    }

    public void deleteList(UUID listId) {
        listRepository.deleteById(listId);
    }

    @Transactional(readOnly = true)
    public UserMarketListingList getList(UUID listId) {
        return listRepository.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List not found with ID: " + listId));
    }

    @Transactional(readOnly = true)
    public List<UserMarketListingList> getUserLists(UUID userId) {
        return listRepository.findByUserId(userId);
    }

    // List item operations
    public UserMarketListingListItem addListingToList(UUID listId, UUID listingId) {
        // Check if already exists
        if (listItemRepository.findByListIdAndListingId(listId, listingId).isPresent()) {
            throw new IllegalArgumentException("Listing already in this list");
        }

        UserMarketListingListItem item = UserMarketListingListItem.builder()
                .listId(listId)
                .listingId(listingId)
                .build();
        return listItemRepository.save(item);
    }

    public void removeListingFromList(UUID listId, UUID listingId) {
        listItemRepository.deleteByListIdAndListingId(listId, listingId);
    }

    @Transactional(readOnly = true)
    public List<UserMarketListingListItem> getListItems(UUID listId) {
        return listItemRepository.findByListId(listId);
    }

    @Transactional(readOnly = true)
    public List<UserMarketListingListItem> getListsContainingListing(UUID listingId) {
        return listItemRepository.findByListingId(listingId);
    }

    @Transactional(readOnly = true)
    public long countListItems(UUID listId) {
        return listItemRepository.countByListId(listId);
    }
}

